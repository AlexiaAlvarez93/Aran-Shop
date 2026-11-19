
package org.example.practicaweb.repository;

import org.example.practicaweb.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByPedido_Id(Long pedidoId);

}