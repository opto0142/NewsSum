package com.newssum.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a promotional code that can unlock premium access.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document(collection = "promo_codes")
public class PromoCode {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    @Field("is_active")
    private boolean active;

    @Field("expires_at")
    private LocalDateTime expiresAt;

    @Field("used_by")
    private String usedBy;

    @Field("used_at")
    private LocalDateTime usedAt;

    @Field("created_by_admin")
    private String createdByAdmin;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    public boolean isExpired(final LocalDateTime referenceTime) {
        return expiresAt != null && expiresAt.isBefore(referenceTime);
    }

    public void markUsed(final String userEmail, final LocalDateTime usedTime) {
        this.active = false;
        this.usedBy = userEmail;
        this.usedAt = usedTime;
    }

    public void reactivate() {
        this.active = true;
        this.usedBy = null;
        this.usedAt = null;
    }
}
