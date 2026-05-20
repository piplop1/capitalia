package com.capitalia.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capitalia.model.CronogramaPago;
import com.capitalia.repository.CronogramaPagoRepository;

@RestController
@RequestMapping("/api/cronograma")
public class CronogramaRestController {

    @Autowired
    private CronogramaPagoRepository cronogramaRepository;

    @GetMapping("/{idPrestamo}")
    public List<CronogramaPago> obtenerCronograma(@PathVariable("idPrestamo") Integer idPrestamo) {
        // Busca las cuotas de ese préstamo y las devuelve como datos JSON al navegador
        return cronogramaRepository.findByPrestamo_IdPrestamoOrderByNumeroCuotaAsc(idPrestamo);
    }
}