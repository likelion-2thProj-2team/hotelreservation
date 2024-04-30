package com.hppystay.hotelreservation.auth.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class CustomRequestMatchers {
    public static AntPathRequestMatcher[] permitAllMatchers = {
            // Auth
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/sign-up"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/sign-in"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/sign-up/send-code"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/email/verify"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/password/send-code"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/password/reset"),

            // Hotel
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotel/areaCode/{area}"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotel/keyword/{keyword}"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotel/location/{mapX}{mapY}"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotel"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotel/{id}"),

            // 호텔 문의
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/hotel/inquiries/list"), //hotel 상세 페이지가 생기고 나면 없앨 것.
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotel/inquiries/{hotelId}"),


            // View
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/login"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/hotel/create-view"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/is-login"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/denied"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/main"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/hotel/search"),



    };

    // 인증된 사용자를 위한 Matcher
    public static AntPathRequestMatcher[] authenticatedMatchers = {
            // Auth
            AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/auth/password/change"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/auth/my-profile"),

            // Hotel
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/profile-upload"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/hotel/reservation"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotel/reservation/my"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotel/reservation/transfer"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/hotel/reservation/transfer"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotel/reservation/transfer/pending"),

            // 호텔 문의
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/hotel/inquiries/submit/{hotelId}"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/hotel/inquiries/submit"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/hotel/inquiries/update/{inquiryId}"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotel/inquiries/update/{inquiryId}"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/hotel/inquiries/update"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/hotel/inquiries/delete/{inquiryId}"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/hotel/inquiries/comments/submit"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotel/inquiries/comments/update/{commentId}"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/hotel/inquiries/comments/update"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/hotel/inquiries/comments/delete/{commentId}"),


            // Like
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/likes/{hotelId}"),

            // view
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/my-page/**"),

    };

    // 일반 USER 를 위한 Matcher
    public static AntPathRequestMatcher[] userMatchers = {
            // Auth
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/manager-requests"),

            // Review
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/{hotelId}/review"),
            AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/{hotelId}/review/{reviewId}/update"),
            AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/{hotelId}/review/{reviewId}/delete"),

            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotel/reservation/transfer/member/search/**"),
            // 고민 부분 (어디까지 허용할지)
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/hotel/reservation/transfer/create"),

            // Toss
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/toss/confirm-payment"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/toss/reservations"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/toss/reservations/{id}/payment"),

            // View
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/hotel/test/paymentComplete"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/hotel/test/paymentFail"),

            // 끝나면 삭제할것
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/hotel/test"),

    };

    // MANAGER 를 위한 Matcher
    public static AntPathRequestMatcher[] managerMatchers = {
            // Hotel
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/hotel"),
            AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/hotel/{id}"),
            AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/hotel/{id}"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/hotel/{id}"),

            // Review
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/{hotelId}/review/{reviewId}"),
    };

    // ADMIN 을 위한 Matcher
    public static AntPathRequestMatcher[] adminMatchers = {
            // Auth
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/auth/manager-requests"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/manager-requests/{requestId}/approve"),
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/manager-requests/{requestId}/reject"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/{hotelId}/review/{reviewId}/delete"),

            // Review
            AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/{hotelId}/review/{reviewId}"),
            AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/{hotelId}/review/{reviewId}/update"),
            AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/{hotelId}/review/{reviewId}/delete"),
    };

    // 정적 자원을 위한 Matcher (인증을 요구하지 않도록 필터링)
    public static AntPathRequestMatcher[] resourcesMatcher = {
            // Resources
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/favicon.ico"),
            AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/static/**"),
    };
}
