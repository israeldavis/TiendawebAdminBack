package com.tiendaweb.admin.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiendaweb.admin.security.SecurityUserAuth;
import com.tiendaweb.admin.user.UserRepository;
import com.tiendaweb.common.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    public static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationFilter.class);
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        logger.info("Username es {}", username);
        logger.info("Password es {}", password);

        if(username != null && password != null) {
            logger.info("Username desde request parameter: " + username);
            logger.info("Password desde request parameter: " + password);
        } else {
            User user = null;
            try{
                user = new ObjectMapper().readValue(request.getInputStream(), User.class);

                username = user.getEmail();
                password = user.getPassword();

                logger.info("Username desde request InputStream (raw): " + username);
                logger.info("Password desde request InputStream (raw): " + password);
            } catch (JsonParseException e) {
                e.printStackTrace();
            }catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, Authentication authentication) throws IOException {
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User)authentication.getPrincipal();
        Algorithm algorithm = Algorithm.HMAC256("secretForTiendaweb2000to20021".getBytes());
        String access_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + (120 * 60 * 1000)))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);

        response.setHeader("jwt-token", access_token);

        User userByEmail = userRepository.findByEmail(user.getUsername());

        SecurityUserAuth securityObject =
                buildUserAuthObject(
                        user,
                        access_token,
                        user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()),
                        userByEmail);

        //Map<String, Object> body = new HashMap<>();
        //body.put("token", access_token);
        //body.put("user", (org.springframework.security.core.userdetails.User) authentication.getPrincipal());
        //body.put("mensaje", String.format("Hola %s, has iniciado sesión con éxito!", user.getUsername()));

        //body.put("securityObject", securityObject);
        //body.put("user", userByEmail);

        response.getWriter().write(new ObjectMapper().writeValueAsString(securityObject));
        response.setStatus(200);
        response.setContentType("application/json");
    }

    private SecurityUserAuth buildUserAuthObject(org.springframework.security.core.userdetails.User user,
                                                 String access_token,
                                                 List<String> roles,
                                                 User userByEmail) {
        SecurityUserAuth securityObject = new SecurityUserAuth();
        securityObject.setUserName(userByEmail.getFullName());
        securityObject.setAuthenticated(true);
        securityObject.setBearerToken(access_token);
        securityObject.setRole(roles.get(0));
        securityObject.setUserImagePath(userByEmail.getPhotosImagePath());
        securityObject.setUserId(userByEmail.getId());

        return securityObject;
    }

}
