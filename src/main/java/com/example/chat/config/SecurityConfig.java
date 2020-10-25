package com.example.chat.config;

import com.example.chat.model.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.PrintWriter;

@Configuration
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final ObjectMapper mapper;
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/session","/index.html","/js/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //开启登录配置
                .authorizeRequests()
                //表示所有接口，登录之后就能访问
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/index.html")
                //登录处理接口
                .loginProcessingUrl("/login")
                //定义登录时，用户名的 key，默认为 username
                .usernameParameter("username")
                //定义登录时，用户密码的 key，默认为 password
                .passwordParameter("password")
                //登录成功的处理器
                .successHandler((req, resp, authentication) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(mapper.writeValueAsString(R.<Void>ok(null)));
                    out.flush();
                })
                //登陆失败的处理器
                .failureHandler((req, resp, exception) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(mapper.writeValueAsString(R.<Void>failed("failed")));
                    out.flush();
                })
                //和表单登录相关的接口可以不受限制直接通过
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                //登出成功的处理器
                .logoutSuccessHandler((req, resp, authentication) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(mapper.writeValueAsString(R.<Void>ok(null)));
                    out.flush();
                })
                .permitAll()
                .and()
                .httpBasic()
                .disable()
                .csrf()
                .disable()
                .userDetailsService(userDetailsService);
    }

    @Bean
    public AuthenticationManager customAuthenticationManager() throws Exception {
        return authenticationManager();
    }
}
