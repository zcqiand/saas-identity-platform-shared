package org.openapitools.configuration;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

import saas.identity.shared.dto.ApiKeyStatus;
import saas.identity.shared.dto.AuditAction;
import saas.identity.shared.dto.MembershipStatus;
import saas.identity.shared.dto.TenantStatus;
import saas.identity.shared.dto.UserStatus;

import jakarta.annotation.Generated;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

/**
 * This class provides Spring Converter beans for the enum models in the OpenAPI specification.
 *
 * By default, Spring only converts primitive types to enums using Enum::valueOf, which can prevent
 * correct conversion if the OpenAPI specification is using an `enumPropertyNaming` other than
 * `original` or the specification has an integer enum.
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-12T14:26:56.554615600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
@Configuration(value = "org.openapitools.configuration.enumConverterConfiguration")
public class EnumConverterConfiguration {

    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.apiKeyStatusConverter")
    Converter<String, ApiKeyStatus> apiKeyStatusConverter() {
        return new Converter<String, ApiKeyStatus>() {
            @Override
            public ApiKeyStatus convert(String source) {
                return ApiKeyStatus.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.auditActionConverter")
    Converter<String, AuditAction> auditActionConverter() {
        return new Converter<String, AuditAction>() {
            @Override
            public AuditAction convert(String source) {
                return AuditAction.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.membershipStatusConverter")
    Converter<String, MembershipStatus> membershipStatusConverter() {
        return new Converter<String, MembershipStatus>() {
            @Override
            public MembershipStatus convert(String source) {
                return MembershipStatus.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.tenantStatusConverter")
    Converter<String, TenantStatus> tenantStatusConverter() {
        return new Converter<String, TenantStatus>() {
            @Override
            public TenantStatus convert(String source) {
                return TenantStatus.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.userStatusConverter")
    Converter<String, UserStatus> userStatusConverter() {
        return new Converter<String, UserStatus>() {
            @Override
            public UserStatus convert(String source) {
                return UserStatus.fromValue(source);
            }
        };
    }

}
