package com.leathric.config;

import com.leathric.entity.Role;
import com.leathric.entity.RoleName;
import com.leathric.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BootstrapDataConfig {

    private final RoleRepository roleRepository;

    @Bean
    CommandLineRunner seedRoles() {
        return args -> {
            if (roleRepository.findByName(RoleName.ROLE_USER).isEmpty()) {
                roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build());
            }
            if (roleRepository.findByName(RoleName.ROLE_ADMIN).isEmpty()) {
                roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build());
            }
        };
    }
}
