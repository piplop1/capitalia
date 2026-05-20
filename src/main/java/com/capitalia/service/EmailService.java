package com.capitalia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    // Inyectamos el correo que configuraste en application.properties para usarlo como remitente
    @Value("${spring.mail.username}")
    private String senderEmail;

    // --- ENVIAR CORREO DE BIENVENIDA ---
    public void sendWelcomeEmail(String to, String name) {
        String subject = "¡Bienvenido a Capitalia!";
        String htmlBody = "<h1>¡Hola, " + name + "!</h1>"
                + "<p>Te damos la bienvenida a Capitalia. Estamos muy contentos de que te unas a nuestra comunidad de ahorro.</p>"
                + "<p>Ya puedes iniciar sesión y empezar a gestionar tus finanzas.</p>"
                + "<p>Saludos,<br>El equipo de Capitalia</p>";

        sendHtmlEmail(to, subject, htmlBody);
    }
    // --- NUEVO: NOTIFICACIÓN DE PRÉSTAMO APROBADO ---
    public void sendLoanApprovalEmail(String to, String name, String monto) {
        String subject = "¡Felicidades! Tu préstamo ha sido aprobado";
        String htmlBody = "<h1>¡Buenas noticias, " + name + "!</h1>"
                + "<p>Nos complace informarte que tu solicitud de préstamo por <strong>S/ " + monto + "</strong> ha sido aprobada.</p>"
                + "<p>El dinero ya ha sido depositado en tu cuenta de Capitalia y está disponible para su uso inmediato.</p>"
                + "<div style='background-color: #e3fcef; padding: 15px; border-radius: 5px; text-align: center; color: #0f5132; margin: 20px 0;'>"
                + "<strong>Estado: DESEMBOLSADO</strong>"
                + "</div>"
                + "<p>Gracias por confiar en nosotros.</p>";

        sendHtmlEmail(to, subject, htmlBody);
    }

    // --- ENVIAR CÓDIGO DE RECUPERACIÓN DE CONTRASEÑA ---
    public void sendPasswordResetCode(String to, String name, String code) {
        String subject = "Tu Código para Restablecer Contraseña en Capitalia";
        String htmlBody = "<h1>¡Hola, " + name + "!</h1>"
                + "<p>Hemos recibido una solicitud para restablecer tu contraseña.</p>"
                + "<p>Usa el siguiente código para continuar. Es válido por 10 minutos:</p>"
                + "<div style='background-color: #f4f4f4; padding: 10px; text-align: center; border-radius: 5px;'>"
                + "<h2 style='color: #333; letter-spacing: 5px; margin: 0;'>" + code + "</h2>"
                + "</div>"
                + "<p>Si no solicitaste esto, puedes ignorar este correo de forma segura.</p>"
                + "<p>Saludos,<br>El equipo de Capitalia</p>";

        sendHtmlEmail(to, subject, htmlBody);
    }

    // --- MÉTODO PRIVADO GENÉRICO PARA ENVIAR EMAILS HTML ---
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            // true indica que es multipart (necesario para enviar HTML)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail); // Remitente (Tu Gmail)
            helper.setTo(to);            // Destinatario (Cualquier correo)
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indica que el contenido es HTML

            javaMailSender.send(message);
            logger.info("Correo enviado exitosamente a: {}", to);

        } catch (MessagingException e) {
            logger.error("Error al enviar correo a {}: {}", to, e.getMessage());
        }
    }
}