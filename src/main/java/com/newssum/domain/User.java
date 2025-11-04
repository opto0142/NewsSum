package com.newssum.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
 * Represents a NewsSum user persisted in MongoDB.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String nickname;

    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @Field("is_premium")
    private boolean premium;

    @Field("promo_code_used")
    private String promoCodeUsed;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    public void updatePassword(final String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateRoles(final List<String> newRoles) {
        this.roles = new ArrayList<>(newRoles);
    }

    public void activatePremium(final String promoCode) {
        this.premium = true;
        this.promoCodeUsed = promoCode;
    }

    public void revokePremium() {
        this.premium = false;
        this.promoCodeUsed = null;
    }
}
