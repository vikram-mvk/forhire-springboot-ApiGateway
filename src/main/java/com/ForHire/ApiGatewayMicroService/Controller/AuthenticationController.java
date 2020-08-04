package com.ForHire.ApiGatewayMicroService.Controller;

import com.ForHire.ApiGatewayMicroService.DTO.JwtResponseDTO;
import com.ForHire.ApiGatewayMicroService.DTO.LoginDTO;
import com.ForHire.ApiGatewayMicroService.DTO.MessageResponseDTO;
import com.ForHire.ApiGatewayMicroService.DTO.SignupDTO;
import com.ForHire.ApiGatewayMicroService.Entity.ENUM_Roles;
import com.ForHire.ApiGatewayMicroService.Entity.Role;
import com.ForHire.ApiGatewayMicroService.Entity.User;
import com.ForHire.ApiGatewayMicroService.Repository.RoleRepository;
import com.ForHire.ApiGatewayMicroService.Repository.UserRepository;
import com.ForHire.ApiGatewayMicroService.Security.JWTUtils.JwtUtils;
import com.ForHire.ApiGatewayMicroService.Security.SpringUtils.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDTO loginDTO) {

        //Get Authenticating Object (containing userDetails) and set it to SecurityContext
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //Create a Token using the Authentication Object
        String jwt = jwtUtils.generateJwtToken(authentication);

        //Get the Authenticated user and find all his roles.
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        //Send it back as response
        return ResponseEntity.ok(new JwtResponseDTO(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupDTO signUp) {

        //Check if user exists
        if (userRepository.existsByUserName(signUp.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Sorry, This Username is already taken."));
        }

        //Check if email already registered
        if (userRepository.existsByEmail(signUp.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("This Email is already registered with us. Please try to login instead"));
        }

        // Create new user's account
        User user = new User();
        user.setUserName(signUp.getUsername());
        user.setEmail(signUp.getEmail());
        user.setPassword(encoder.encode(signUp.getPassword()));

        Set<String> strRoles = signUp.getRole();
        Set<Role> roles = new HashSet<>();
        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ENUM_Roles.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ENUM_Roles.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ENUM_Roles.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ENUM_Roles.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponseDTO("User registered successfully!"));
    }

}


