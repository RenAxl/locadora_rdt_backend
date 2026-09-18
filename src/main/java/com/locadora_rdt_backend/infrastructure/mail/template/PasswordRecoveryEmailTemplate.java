package com.locadora_rdt_backend.infrastructure.mail.template;

import org.springframework.stereotype.Service;

@Service
public class PasswordRecoveryEmailTemplate {

    public String buildTemplate(
            String name,
            String link,
            long minutes
    ) {

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "</head>" +
                "<body style='font-family: Arial, sans-serif;'>" +

                "<h2>Recuperação de senha 🔐</h2>" +

                "<p>Olá, <b>" + escape(name) + "</b>!</p>" +

                "<p>" +
                "Recebemos uma solicitação para redefinir sua senha. " +
                "Clique no botão abaixo:" +
                "</p>" +

                "<p style='margin: 24px 0;'>" +
                "<a href='" + link + "' " +
                "style='" +
                "background:#0d6efd;" +
                "color:#fff;" +
                "padding:12px 18px;" +
                "text-decoration:none;" +
                "border-radius:6px;" +
                "'>" +
                "Redefinir minha senha" +
                "</a>" +
                "</p>" +

                "<p>" +
                "Este link expira em " +
                "<b>" + minutes + " minutos</b>." +
                "</p>" +

                "<p>" +
                "Se você não solicitou, pode ignorar este e-mail." +
                "</p>" +

                "<p>" +
                "Equipe <b>Locadora RDT</b>" +
                "</p>" +

                "</body>" +
                "</html>";
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}