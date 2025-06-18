package com.wms.warehouse.controller;

import com.wms.warehouse.entity.Role;
import com.wms.warehouse.entity.User;
import com.wms.warehouse.entity.UserView;
import com.wms.warehouse.repository.RoleRepository;
import com.wms.warehouse.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String listUsers(Model model) {
        List<User> allUsers = userRepository.findAll();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SUPER_ADMIN"));
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        List<UserView> userViews = allUsers.stream().map(user -> {
            boolean isTargetSuperAdmin = user.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("ROLE_SUPER_ADMIN"));
            boolean isTargetAdmin = user.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
            boolean isSelf = currentUser.getId().equals(user.getId());

            boolean canEdit = false;
            boolean canDelete = false;

            if (isSuperAdmin) {
                canEdit = true; // может редактировать всех, включая себя
                canDelete = !isSelf; // всех, кроме самого себя
            } else if (isAdmin) {
                if (isSelf) {
                    canEdit = true; // может редактировать только самого себя
                    canDelete = false;
                } else if (!isTargetAdmin && !isTargetSuperAdmin) {
                    canEdit = true;
                    canDelete = true;
                }
            }

            return new UserView(user, canEdit, canDelete);
        }).toList();

        model.addAttribute("users", userViews);
        return "users";
    }



    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());
        return "user_form";
    }

    @PostMapping
    public String createUser(@ModelAttribute User user, @RequestParam String roleName) {
        Role role = roleRepository.findByName(roleName);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(new HashSet<>(Set.of(role)));

        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());
        return "user_form";
    }

    @PostMapping("/{id}/update")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User updated,
                             @RequestParam String roleName) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден"));

        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SUPER_ADMIN"));
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        boolean isTargetSuperAdmin = targetUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SUPER_ADMIN"));
        boolean isTargetAdmin = targetUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        boolean isSelf = currentUser.getId().equals(targetUser.getId());

        // Логика прав на редактирование
        if (isSuperAdmin) {
            // может редактировать всех, включая себя
        } else if (isAdmin) {
            if (isSelf) {
                // может редактировать только себя
            } else if (isTargetAdmin || isTargetSuperAdmin) {
                throw new SecurityException("Недостаточно прав для редактирования другого администратора.");
            }
        } else {
            throw new SecurityException("Недостаточно прав.");
        }

        Role role = roleRepository.findByName(roleName);
        targetUser.setUsername(updated.getUsername());
        targetUser.setRoles(new HashSet<>(Set.of(role)));

        if (!updated.getPassword().isBlank()) {
            targetUser.setPassword(passwordEncoder.encode(updated.getPassword()));
        }

        userRepository.save(targetUser);
        return "redirect:/admin/users";
    }



    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден"));

        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SUPER_ADMIN"));
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        boolean isTargetSuperAdmin = targetUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SUPER_ADMIN"));
        boolean isTargetAdmin = targetUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        boolean isSelf = currentUser.getId().equals(targetUser.getId());

        // Логика прав на удаление
        if (isSelf) {
            throw new SecurityException("Нельзя удалить самого себя.");
        }

        if (isSuperAdmin) {
            // может удалить всех, кроме себя (уже проверено выше)
        } else if (isAdmin) {
            if (isTargetAdmin || isTargetSuperAdmin) {
                throw new SecurityException("Недостаточно прав для удаления администратора.");
            }
        } else {
            throw new SecurityException("Недостаточно прав.");
        }

        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }


}
