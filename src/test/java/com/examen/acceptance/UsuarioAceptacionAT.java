package com.examen.acceptance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.examen.UsuarioRepository;
import com.examen.UsuarioService;

// Prueba de aceptacion: valida un criterio de negocio de punta a punta,
// no un detalle tecnico interno como las pruebas unitarias o de integracion.
public class UsuarioAceptacionAT {

    @Test
    public void unUsuarioRegistradoDebeQuedarDisponibleParaConsulta() {
        // Given: un servicio de usuarios recien creado
        UsuarioService service = new UsuarioService(new UsuarioRepository());

        // When: se registra un nuevo usuario
        boolean registrado = service.registrar("Esteban", "esteban@iplacex.cl");

        // Then: el registro debe confirmarse y el usuario debe quedar disponible para consulta
        assertTrue(registrado);
        assertEquals("esteban@iplacex.cl", service.obtenerEmail("Esteban"));
    }
}