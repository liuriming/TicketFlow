package com.ticketflow.ticket.domain;

import com.ticketflow.ticket.entity.Ticket;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TicketDataScopeCriteriaTest {

    @Test
    void selfScopeOnlyMatchesTicketsCreatedByCurrentUser() {
        TicketDataScopeCriteria criteria = TicketDataScopeCriteria.self(8L);

        assertThat(criteria.matches(ticket(8L, 20L))).isTrue();
        assertThat(criteria.matches(ticket(9L, 20L))).isFalse();
    }

    @Test
    void deptScopeMatchesTicketsCreatedByConfiguredDepartments() {
        TicketDataScopeCriteria criteria = TicketDataScopeCriteria.depts(List.of(11L, 12L, 13L));

        assertThat(criteria.matches(ticket(8L, 12L, null))).isTrue();
        assertThat(criteria.matches(ticket(8L, 30L, null))).isFalse();
    }

    @Test
    void assignedTicketCanBeAccessedEvenWhenCreatorDeptIsOutOfScope() {
        TicketDataScopeCriteria criteria = TicketDataScopeCriteria.depts(List.of(12L), 8L);

        assertThat(criteria.matches(ticket(9L, 30L, 8L))).isTrue();
        assertThat(criteria.matches(ticket(9L, 30L, 10L))).isFalse();
    }

    @Test
    void allScopeMatchesEveryTicket() {
        TicketDataScopeCriteria criteria = TicketDataScopeCriteria.all();

        assertThat(criteria.matches(ticket(8L, 12L, null))).isTrue();
        assertThat(criteria.matches(ticket(9L, null, null))).isTrue();
    }

    private Ticket ticket(Long creatorId, Long creatorDeptId) {
        return ticket(creatorId, creatorDeptId, null);
    }

    private Ticket ticket(Long creatorId, Long creatorDeptId, Long assigneeId) {
        Ticket ticket = new Ticket();
        ticket.setCreatorId(creatorId);
        ticket.setCreatorDeptId(creatorDeptId);
        ticket.setAssigneeId(assigneeId);
        return ticket;
    }
}
