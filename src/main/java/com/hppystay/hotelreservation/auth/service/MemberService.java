package com.hppystay.hotelreservation.auth.service;

import com.hppystay.hotelreservation.auth.dto.CreateMemberDto;
import com.hppystay.hotelreservation.auth.dto.MemberDto;
import com.hppystay.hotelreservation.auth.dto.PasswordChangeRequestDto;
import com.hppystay.hotelreservation.auth.dto.PasswordDto;
import com.hppystay.hotelreservation.auth.entity.*;
import com.hppystay.hotelreservation.auth.jwt.JwtRequestDto;
import com.hppystay.hotelreservation.auth.jwt.JwtResponseDto;
import com.hppystay.hotelreservation.auth.jwt.JwtTokenUtils;
import com.hppystay.hotelreservation.auth.repository.VerificationRepository;
import com.hppystay.hotelreservation.auth.repository.MemberRepository;
import com.hppystay.hotelreservation.common.exception.GlobalErrorCode;
import com.hppystay.hotelreservation.common.exception.GlobalException;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final VerificationRepository verificationRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;

    // 회원 가입
    @Transactional
    public MemberDto signUp(CreateMemberDto createMemberDto) {
        // 이메일 중복 체크
        if (userExists(createMemberDto.getEmail()))
            throw new GlobalException(GlobalErrorCode.EMAIL_ALREADY_EXISTS);

        EmailVerification verification = verificationRepository.findByEmail(createMemberDto.getEmail())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.VERIFICATION_NOT_FOUND));
        if (!verification.getStatus().equals(VerificationStatus.VERIFIED)) {
            // 상태가 적절하지 않은 경우
            throw new GlobalException(GlobalErrorCode.VERIFICATION_INVALID_STATUS);
        }

        Member member = Member.builder()
                .nickname(createMemberDto.getNickname())
                .email(createMemberDto.getEmail())
                .password(passwordEncoder.encode(createMemberDto.getPassword()))
                .role(MemberRole.ROLE_USER)
                .build();

        verificationRepository.deleteByEmail(createMemberDto.getEmail());

        return MemberDto.fromEntity(memberRepository.save(member));
    }

    // 해당 이메일로 가입한 아이디 확인
    public boolean userExists(String email) {
        return memberRepository.existsByEmail(email);
    }

    //로그인 (jwt 토큰 발급)
    public JwtResponseDto issueToken(JwtRequestDto dto) {
        Optional<Member> optionalMember = memberRepository.findMemberByEmail(dto.getEmail());
        if (optionalMember.isEmpty())
            throw new GlobalException(GlobalErrorCode.EMAIL_PASSWORD_MISMATCH);

        Member member = optionalMember.get();

        // 비밀번호 같은지 확인
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword()))
            throw new GlobalException(GlobalErrorCode.EMAIL_PASSWORD_MISMATCH);

        String jwt = jwtTokenUtils.generateAccessToken(member);
        JwtResponseDto response = new JwtResponseDto();
        response.setToken(jwt);

        return response;
    }


    @Value("${SMTP_USERNAME}")
    private String senderEmail;

    @Transactional
    public void sendVerifyCode(String receiverEmail) {
        String verifyCode = generateRandomNumber(6);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setFrom(senderEmail);
            message.setRecipients(Message.RecipientType.TO, receiverEmail);

            message.setSubject("[RESTful Lion] 인증 코드입니다.");

            StringBuilder textBody = new StringBuilder();
            textBody.append("<div style=\"max-width: 600px;\">")
                    .append("   <h2>Email Verification</h2>")
                    .append("   <p>아래의 인증 코드를 사용하여 이메일 주소를 인증해주세요.</p>")
                    .append("   <p><strong>인증 코드:</strong> <span style=\"font-size: 18px; font-weight: bold;\">")
                    .append(verifyCode)
                    .append("</span></p>")
                    .append("   <p>이 코드는 5분간 유효합니다.</p>")
                    .append("   <p>감사합니다.</p>")
                    .append("</div>");
            message.setText(textBody.toString(), "UTF-8", "HTML");

            javaMailSender.send(message);
        } catch (Exception e) {
            throw new GlobalException(GlobalErrorCode.EMAIL_SENDING_FAILED);
        }

        // 기존 진행중인 인증 코드 발송 내역은 삭제
        verificationRepository.deleteByEmail(receiverEmail);

        EmailVerification verification = EmailVerification.builder()
                .email(receiverEmail)
                .verifyCode(verifyCode)
                .status(VerificationStatus.SENT)
                .build();

        // 인증 코드 발송 내역 저장
        verificationRepository.save(verification);
    }

    public void verifyEmail(String email, String code) {
        EmailVerification verification = verificationRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.VERIFICATION_NOT_FOUND));

        if (!verification.getVerifyCode().equals(code)) {
            // 이메일로 전송된 인증 코드와 DB에 저장된 인증 코드가 일치하는지 확인
            throw new GlobalException(GlobalErrorCode.VERIFICATION_CODE_MISMATCH);
        } else if (!verification.getStatus().equals(VerificationStatus.SENT)) {
            // 상태가 적절하지 않은 경우
            throw new GlobalException(GlobalErrorCode.VERIFICATION_INVALID_STATUS);
        } else if (verification.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
            // 인증 시간 만료
            throw new GlobalException(GlobalErrorCode.VERIFICATION_EXPIRED);
        }

        verification.setStatus(VerificationStatus.VERIFIED);
        verificationRepository.save(verification);
    }

    // 랜덤 숫자코드를 생성하는 메서드
    public String generateRandomNumber(int len) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    @Transactional
    public ResponseEntity<String> signUpSendCode(String email) {
        if (userExists(email))
            throw new GlobalException(GlobalErrorCode.EMAIL_ALREADY_EXISTS);
        sendVerifyCode(email);
        return ResponseEntity.ok("{}");
    }

    @Transactional
    public ResponseEntity<String> passwordSendCode(String email) {
        // email 이 존재하는 멤버인지 확인
        if (!userExists(email))
            throw new GlobalException(GlobalErrorCode.EMAIL_NOT_FOUND);
        sendVerifyCode(email);
        return ResponseEntity.ok("{}");
    }

    // 비밀번호 인증 코드 확인 메서드
    @Transactional
    public ResponseEntity<String> resetPassword(PasswordChangeRequestDto requestDto) {
        String email = requestDto.getEmail();
        String code = requestDto.getCode();
        String newPassword = requestDto.getNewPassword();

        EmailVerification verification = verificationRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.VERIFICATION_NOT_FOUND));

        if (!verification.getVerifyCode().equals(code)) {
            // 이메일로 전송된 인증 코드와 DB에 저장된 인증 코드가 일치하는지 확인
            throw new GlobalException(GlobalErrorCode.VERIFICATION_CODE_MISMATCH);
        } else if (!verification.getStatus().equals(VerificationStatus.VERIFIED)) {
            // 상태가 적절하지 않은 경우
            throw new GlobalException(GlobalErrorCode.VERIFICATION_INVALID_STATUS);
        }

        Member member = memberRepository.findMemberByEmail(email).orElseThrow(
                () -> new GlobalException(GlobalErrorCode.EMAIL_NOT_FOUND));
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);

        verificationRepository.deleteByEmail(email);

        return ResponseEntity.ok("Success");
    }


    // 비밀번호 변경 메서드
    @Transactional
    public ResponseEntity<String> changePassword(PasswordDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication.getName();
        log.info(currentUser);

        Optional<Member> optionalMember = memberRepository.findMemberByEmail(currentUser);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();

            //비밀번호 일치 여부 확인
            if (!passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
                throw new GlobalException(GlobalErrorCode.PASSWORD_MISMATCH);
            }

            member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            memberRepository.save(member);
            log.info(member.getEmail());
        }
        return ResponseEntity.ok("New password");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Member> optionalMember = memberRepository.findMemberByEmail(username);

        if (optionalMember.isEmpty())
            throw new GlobalException(GlobalErrorCode.EMAIL_NOT_FOUND);
        Member member = optionalMember.get();

        return CustomUserDetails.builder()
                .member(member)
                .build();
    }
}
