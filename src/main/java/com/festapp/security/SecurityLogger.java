package com.festapp.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SecurityLogger {

    private static final Logger log = LoggerFactory.getLogger(SecurityLogger.class);

    public void loginSucesso(String email, String ip) {
        log.info("LOGIN OK | email={} | ip={}", email, ip);
    }

    public void loginFalhou(String email, String ip) {
        log.warn("LOGIN FALHOU | email={} | ip={}", email, ip);
    }

    public void acessoNegado(String ip, String rota) {
        log.warn("ACESSO NEGADO | ip={} | rota={}", ip, rota);
    }

    public void rateLimitAtingido(String ip) {
        log.warn("RATE LIMIT | ip={} | muitas tentativas de login", ip);
    }
}