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
        ensureRoleExists("Admin", "Administrator");
        ensureRoleExists("Owner", "Building Owner");
        ensureRoleExists("Staff", "Staff");
        ensureRoleExists("Tenant", "Tenant");

        if (userRepository.findByUsername("admin").isEmpty()) {
            roleRepository.findByRoleName("Admin").ifPresent(adminRole -> {
                User admin = User.builder()
                        .username("admin")
                        .passwordHash(passwordEncoder.encode("admin"))
                        .role(adminRole)
                        .isActive(true)
                        .isDeleted(false)
                        .build();
                userRepository.save(admin);
                log.info("Admin user initialized: username=admin, password=admin");
            });
        }
    }

    private void ensureRoleExists(String roleName, String description) {
        if (roleRepository.findByRoleName(roleName).isEmpty()) {
            roleRepository.save(Role.builder()
                    .roleName(roleName)
                    .description(description)
                    .build());
            log.info("Role initialized: {}", roleName);
        }
    }
}
