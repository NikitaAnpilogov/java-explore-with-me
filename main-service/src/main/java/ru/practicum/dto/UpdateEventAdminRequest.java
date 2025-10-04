package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.model.enums.EventAdminState;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest extends UpdateEventRequest {

    private EventAdminState stateAction;
}