package fr.snapwedding.event.internal;

import fr.snapwedding.event.EventStatus;
import fr.snapwedding.shared.InvalidRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests unitaires des règles de l'entité : pas de Spring, pas de base de données. */
class EventTest {

    private static Event newEvent() {
        return new Event("alice", "Mariage", LocalDate.of(2027, 6, 12), "Toulouse", "code");
    }

    @Test
    void aNewEventIsOpen() {
        assertThat(newEvent().getStatus()).isEqualTo(EventStatus.OPEN);
        assertThat(newEvent().toView().isOpen()).isTrue();
    }

    @Test
    void closingAnEventStopsUploads() {
        Event event = newEvent();
        event.close();
        assertThat(event.getStatus()).isEqualTo(EventStatus.CLOSED);
        assertThat(event.toView().isOpen()).isFalse();
    }

    @Test
    void aSuspendedEventCannotBeClosedByTheOrganizer() {
        Event event = newEvent();
        event.suspend();
        assertThatThrownBy(event::close).isInstanceOf(InvalidRequestException.class);
        assertThat(event.getStatus()).isEqualTo(EventStatus.SUSPENDED);
    }

    @Test
    void reactivatingReopensOnlyASuspendedEvent() {
        Event event = newEvent();
        assertThatThrownBy(event::reactivate).isInstanceOf(InvalidRequestException.class);

        event.suspend();
        event.reactivate();
        assertThat(event.getStatus()).isEqualTo(EventStatus.OPEN);
    }

    @Test
    void publicCodesAre128BitsAndUnique() {
        PublicCodeGenerator generator = new PublicCodeGenerator();
        String a = generator.generate();
        String b = generator.generate();
        assertThat(a).hasSize(22).isNotEqualTo(b); // 16 octets en Base64 URL sans "=" = 22 caractères
    }
}
