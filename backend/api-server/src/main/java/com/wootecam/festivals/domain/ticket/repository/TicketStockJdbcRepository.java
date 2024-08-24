package com.wootecam.festivals.domain.ticket.repository;

import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TicketStockJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void saveTicketStocks(List<TicketStock> ticketStocks) {
        String sql = "INSERT INTO ticket_stock (ticket_id) VALUES (?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int index) throws SQLException {
                TicketStock ticketStock = ticketStocks.get(index);
                ps.setLong(1, ticketStock.getTicket().getId());
            }

            @Override
            public int getBatchSize() {
                return ticketStocks.size();
            }
        });
    }
}
