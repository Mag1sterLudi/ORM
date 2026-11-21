package com.learning.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Дополнительная информация о пользователе (1-1 с User) */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "profiles")
public class Profile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 2000)
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 30)
    private String phone;

    public Profile(String bio, String avatarUrl, String phone) {
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.phone = phone;
    }
}
