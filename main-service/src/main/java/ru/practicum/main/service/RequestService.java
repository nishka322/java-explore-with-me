package ru.practicum.main.service;

import ru.practicum.main.dto.request.RequestDto;
import ru.practicum.main.dto.request.RequestStatusUpdateDto;
import ru.practicum.main.dto.request.RequestStatusUpdateResult;
import ru.practicum.main.enumeration.RequestStatus;
import ru.practicum.main.model.Event;

import java.util.List;
import java.util.Map;

public interface RequestService {
    RequestDto createRequest(Long userId, Long eventId);

    List<RequestDto> getRequestsByOwnerOfEvent(Long userId, Long eventId);

    RequestStatusUpdateResult updateRequests(Long userId, Long eventId, RequestStatusUpdateDto requestStatusUpdateDto);

    List<RequestDto> getCurrentUserRequests(Long userId);

    RequestDto cancelRequests(Long userId, Long requestId);

    Map<Long, Long> getConfirmedRequestsCountForEvents(List<Event> events);

    Long getConfirmedRequestsCountForEvent(Long eventId);
}