package com.group10.API_ManageDormitory.config.key;

import com.group10.API_ManageDormitory.entity.Role;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.repository.RoleRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            roleRepository.save(Role.builder().roleName("ADMIN").description("Administrator").build());
            roleRepository.save(Role.builder().roleName("OWNER").description("Building Owner").build());
            roleRepository.save(Role.builder().roleName("STAFF").description("Staff").build());
            roleRepository.save(Role.builder().roleName("TENANT").description("Tenant").build());
            log.info("Roles initialized: ADMIN, OWNER, STAFF, TENANT");
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByRoleName("ADMIN").orElse(null);
            if (adminRole != null) {
                User admin = User.builder()
                        .username("admin")
                        .passwordHash(passwordEncoder.encode("admin"))
                        .role(adminRole)
                        .isActive(true)
                        .isDeleted(false)
                        .build();
                userRepository.save(admin);
                log.info("Admin user initialized: username=admin, password=admin");
            }
        }
    }
}
