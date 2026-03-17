package com.kh.mallapi.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kh.mallapi.domain.Member;
import com.kh.mallapi.domain.MemberRole;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class MemberRepositoryTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	//@Test
	public void testInsertMember() {
		for (int i = 0; i < 10; i++) {
			// user0~user4 권한 USER
			Member member = Member.builder().email("user" + i + "@jjjj.com").pw(passwordEncoder.encode("123456"))
					.nickname("USER" + i).build();
			member.addRole(MemberRole.USER);
			// user5~user8 권한 USER, MANAGER
			if (i >= 5) {
				member.addRole(MemberRole.MANAGER);
			}
			// user9 권한 USER, MANAGER, ADMIN
			if (i >= 9) {
				member.addRole(MemberRole.ADMIN);
			}
			memberRepository.save(member);
		}
	}

	@Test
	public void testRead() {
		String email = "user9@jjjj.com";
		Member member = memberRepository.getWithRoles(email);
		log.info("member =" + member);
		log.info(member.getMemberRoleList());
	}
}
