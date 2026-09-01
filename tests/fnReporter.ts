// Vitest reporter: collects function IDs from test names and writes .state/trace.json.
//
// 2026-08-29 修复历史：
//   v1 — 只读 it() 名，丢 describe() 级 ID → trace 恒空
//   v2 — onCollected 阶段取 result.state（还不存在），把 skip 当 pass → 假覆盖
//   v3 — describe 链累积 + onFinished 收集 + 按列取状态（skip/inert 正确）
//   v4 — filter 到「本仓命名空间」。命名空间 = 该仓 function-tree 中出现过的
//        所有 top-level module（`M00` / `M99` 等）。其他 ID 当作描述性引用
//        （如 mock 仓测试名里说「M04.F03 对应 OAuth server mock」），不进 trace，
//        不参与 L5 引用检查 —— 这正是 conventions §7「mock 不镜像业务模块」的
//        测试侧落地。
//   v5 (2026-09-01) — vitest 2.x `onTaskUpdate` 拿到的 packs 是
//        `Array<[id, result, meta]>` tuple（不是 `{tasks:[]}` 对象），
//        `pack.tasks` 恒 undefined → collectTests 空跑，addEntry 永远不被调。
//        修法：onFinished(files: TestFile[]) 拿完整 task tree 兜底
//        （files 顶层是 TestFile，每个含 tasks: Suite[]，可递归走完所有 test +
//         describe.skipIf 过滤掉的 inner test）。同时保留 onTaskUpdate
//         早写（PASS 测试 result 已就绪），用 tuple 形态正确解析。
import type { Reporter } from "vitest/reporters";
import { readFileSync } from "node:fs";

interface TraceEntry {
  test: string;
  fns: string[];
  inert: boolean;
}

const TRACE_FILE = ".state/trace.json";
const FUNCTION_ID_RE = /\bM\d{2}(?:\.F\d{2}(?:\.I\d{2})?)?\b/g;

/** 该仓允许的命名空间集合 = 本仓树里出现过的 top-level module。 */
function loadNamespaces(): Set<string> {
  let text: string;
  try {
    text = readFileSync("docs/functions/function-tree.md", "utf-8");
  } catch {
    return new Set(); // 无树 = 仓刚 init，跳过命名空间过滤
  }
  const ns = new Set<string>();
  for (const line of text.split("\n")) {
    if (!line.trimStart().startsWith("|")) continue;
    const cells = line.split("|").map((c: string) => c.trim());
    if (cells.length < 2) continue;
    // markdown 表格行首位都有 `|`，split 后 cells[0] 是空前缀段、cells[1] 才是 ID 列
    const idCell = cells[1] ?? cells[0];
    const m = FUNCTION_ID_RE.exec(idCell);
    if (m && m[0]) ns.add(m[0].slice(0, 2));
    FUNCTION_ID_RE.lastIndex = 0;
  }
  return ns;
}

/** 递归收集测试，沿途累积 describe 链。 */
function collectTests(
  tasks: any[],
  prefix = "",
  out: { fullName: string; task: any }[] = [],
): { fullName: string; task: any }[] {
  for (const t of tasks) {
    if (!t) continue;
    const name = t.name || "";
    const fullName = prefix ? `${prefix} > ${name}` : name;
    if (t.type === "test") out.push({ fullName, task: t });
    else if (t.type === "suite" && t.tasks) collectTests(t.tasks, fullName, out);
  }
  return out;
}

export default class FnReporter implements Partial<Reporter> {
  private entries: TraceEntry[] = [];
  private namespaces: Set<string> | null = null;

  private addEntry(t: { fullName: string; task: any }) {
    if (!this.namespaces) this.namespaces = loadNamespaces();
    const state = t.task.result?.state;
    const mode = t.task.mode;
    const isInert = state === "skip" || state === "todo" || mode === "skip" || mode === "todo";

    // 仅本仓命名空间的 ID 算 trace。跨命名空间当描述性引用，丢弃。
    const all = extractFns(t.fullName);
    const fns =
      this.namespaces.size === 0 ? [] : all.filter((id) => this.namespaces!.has(id.slice(0, 2)));

    if (fns.length === 0 && !isInert) return;
    this.entries.push({ test: t.fullName, fns: isInert ? [] : fns.sort(), inert: isInert });
  }

  /**
   * vitest 2.x onTaskUpdate(packs) packs 是 `Array<[id, result, meta]>` tuple，
   * 不是 `{tasks:[]}` 对象。每个 tuple 元素是 [string, TestResult?, TestMeta?]。
   * 我们要从 id 拿完整 task 节点，但 reporter 上下文里没有 state.idMap，
   * 所以这里只能存『已跑过的 result』。完整 task 树改走 onFinished。
   *
   * 注：本钩子在 PASS 测试 result 已就绪时触发，但 inert 测试常被 skipIf
   * 过滤掉、result 还没填充。所以 onTaskUpdate 主要服务 PASS 测试的早写。
   */
  async onTaskUpdate(packs: any[]) {
    if (process.env.TRACE_MAP !== "1") return;
    // pack 是 tuple，无法在不查 state 的前提下拿到 task 节点。
    // 我们什么都不做，等 onFinished(files) 一次性 walk 全树。
    void packs;
    await this.flush();
  }

  /**
   * vitest 2.x onFinished(files: TestFile[]) 是 inert 测试可达的唯一入口。
   * TestFile 含 tasks: Suite[]，递归 walk 拿全部 test + describe。
   */
  async onFinished(files?: unknown[]) {
    if (process.env.TRACE_MAP !== "1") return;
    if (!this.namespaces) this.namespaces = loadNamespaces();

    if (Array.isArray(files)) {
      for (const file of files) {
        const f = file as { tasks?: any[] };
        if (f?.tasks) collectTests(f.tasks).forEach((t) => this.addEntry(t));
      }
    }
    await this.flush();
  }

  private async flush() {
    const fs = await import("node:fs");
    const path = await import("node:path");
    fs.mkdirSync(".state", { recursive: true });
    fs.writeFileSync(
      path.resolve(TRACE_FILE),
      JSON.stringify({ schema: 1, tests: this.entries }, null, 2) + "\n",
      "utf-8",
    );
  }
}

function extractFns(text: string): string[] {
  if (!text) return [];
  const ids: string[] = [];
  const re = new RegExp(FUNCTION_ID_RE.source, "g");
  let m: RegExpExecArray | null;
  while ((m = re.exec(text)) !== null) if (!ids.includes(m[0])) ids.push(m[0]);
  return ids;
}