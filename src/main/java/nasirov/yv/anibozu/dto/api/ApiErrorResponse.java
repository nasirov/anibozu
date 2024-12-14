package nasirov.yv.anibozu.dto.api;

import jakarta.validation.constraints.NotNull;

public record ApiErrorResponse(@NotNull String errorMessage) {}
