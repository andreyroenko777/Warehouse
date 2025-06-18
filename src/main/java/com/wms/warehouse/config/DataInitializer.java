package com.wms.warehouse.config;

import com.wms.warehouse.entity.Role;
import com.wms.warehouse.entity.User;
import com.wms.warehouse.repository.RoleRepository;
import com.wms.warehouse.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner init(RoleRepository roleRepo,
                                  UserRepository userRepo,
                                  PasswordEncoder passwordEncoder) {
        return args -> {
            // Создание ролей (если их нет)
            if (roleRepo.findByName("ROLE_SUPER_ADMIN") == null)
                roleRepo.save(new Role("ROLE_SUPER_ADMIN"));
            if (roleRepo.findByName("ROLE_ADMIN") == null)
                roleRepo.save(new Role("ROLE_ADMIN"));
            if (roleRepo.findByName("ROLE_MANAGER") == null)
                roleRepo.save(new Role("ROLE_MANAGER"));
            if (roleRepo.findByName("ROLE_WAREHOUSE") == null)
                roleRepo.save(new Role("ROLE_WAREHOUSE"));

            // Если в системе НЕТ пользователей, создаём SUPER_ADMIN
            if (userRepo.count() == 0) {
                Role superAdminRole = roleRepo.findByName("ROLE_SUPER_ADMIN");
                User superAdmin = new User();
                superAdmin.setUsername("superadmin");
                superAdmin.setPassword(passwordEncoder.encode("admin123"));
                superAdmin.setRoles(Set.of(superAdminRole));
                userRepo.save(superAdmin);

                System.out.println("✅ SUPER_ADMIN 'superadmin' создан с паролем 'admin123'");
            }
        };
    }
}
