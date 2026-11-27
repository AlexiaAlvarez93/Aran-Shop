package org.example.practicaweb.repository;

import org.example.practicaweb.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuario_Id(Long usuarioId);

}