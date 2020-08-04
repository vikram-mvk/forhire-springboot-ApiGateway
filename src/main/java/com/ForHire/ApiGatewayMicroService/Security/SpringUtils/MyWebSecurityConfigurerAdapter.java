package com.ForHire.ApiGatewayMicroService.Security.SpringUtils;

import com.ForHire.ApiGatewayMicroService.Security.JWTUtils.MyOncePerRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(   //More about Method security -> https://www.baeldung.com/spring-security-method-security
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
public class MyWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
    @Autowired private MyUserDetailsService MyUserDetailsService;
    @Autowired private UnAuthorizedHandler unauthorizedHandler;


    @Override  //AuthenticationManagerBuilder is used for building a custom Authentication for your app
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(MyUserDetailsService).passwordEncoder(getPasswordEncoder());  }



    @Override     //HttpSecurity is used for Authorization
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests().antMatchers("/auth/**").permitAll().antMatchers("/actuator/**").permitAll().antMatchers("/hystrix/**").permitAll()
                .anyRequest().authenticated();
        httpSecurity.addFilterBefore(getMyFilter(), UsernamePasswordAuthenticationFilter.class);
    }


    //Beans
    @Bean //Declare it as a bean to be used by the controller
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {   return super.authenticationManagerBean(); }

    @Bean // Used by Auth to encode
    public PasswordEncoder getPasswordEncoder() {  return new BCryptPasswordEncoder();   }

    @Bean
    public MyOncePerRequestFilter getMyFilter(){ return new MyOncePerRequestFilter(); }


}