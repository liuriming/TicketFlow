package com.ticketflow.common.config;

import com.ticketflow.audit.service.OperationLogService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class OperationLogInterceptorTest {

    private final OperationLogService operationLogService = mock(OperationLogService.class);
    private final OperationLogInterceptor interceptor = new OperationLogInterceptor(operationLogService);

    @Test
    void 变更类Api请求完成后会记录操作日志() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/tickets");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        verify(operationLogService).record(request, response, null);
    }

    @Test
    void 查询请求不会记录操作日志() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tickets");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        verify(operationLogService, never()).record(request, response, null);
    }
}
