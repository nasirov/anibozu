package nasirov.yv.anibozu.dto.api;

import jakarta.validation.constraints.NotNull;

/**
 * @author Nasirov Yuriy
 */
public record ApiErrorResponse(@NotNull String errorMessage) {}