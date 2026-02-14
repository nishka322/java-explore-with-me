package ru.practicum.main.utils;

import org.springframework.stereotype.Component;
import ru.practicum.main.exception.PaginationException;

@Component
public class PaginationValidator {

    public void validatePaginationParams(Integer from, Integer size) {
        if (from == null || size == null) {
            throw new PaginationException("Parameters 'from' and 'size' must not be null");
        }
        if (size <= 0) {
            throw new PaginationException("Parameter 'size' must be positive");
        }
        if (from < 0) {
            throw new PaginationException("Parameter 'from' must be zero or positive");
        }
        if (from % size != 0) {
            throw new PaginationException("Parameter 'from' must be a multiple of 'size'");
        }
    }
}
