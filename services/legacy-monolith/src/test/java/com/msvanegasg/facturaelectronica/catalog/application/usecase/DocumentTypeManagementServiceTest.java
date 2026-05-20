package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.catalog.application.dto.DocumentTypeCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.DocumentTypeRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.DocumentType;
import com.msvanegasg.facturaelectronica.exception.tipodocumento.TipoDocumentoNotFoundException;

class DocumentTypeManagementServiceTest {

    @Test
    void createDocumentTypeStartsActiveAndPersistsIt() {
        InMemoryDocumentTypeRepository repository = new InMemoryDocumentTypeRepository();
        DocumentTypeManagementService service = new DocumentTypeManagementService(repository);

        DocumentType documentType = service.create(new DocumentTypeCommand(13L, "Cedula", "Cedula ciudadania"));

        assertThat(documentType.code()).isEqualTo(13L);
        assertThat(documentType.name()).isEqualTo("Cedula");
        assertThat(documentType.description()).isEqualTo("Cedula ciudadania");
        assertThat(documentType.active()).isTrue();
    }

    @Test
    void updateDocumentTypeKeepsCodeAndActiveState() {
        InMemoryDocumentTypeRepository repository = new InMemoryDocumentTypeRepository();
        repository.save(DocumentType.restore(13L, "Cedula", "Inicial", true));
        DocumentTypeManagementService service = new DocumentTypeManagementService(repository);

        DocumentType updated = service.update(13L, new DocumentTypeCommand(31L, "NIT", "Tributario"));

        assertThat(updated.code()).isEqualTo(13L);
        assertThat(updated.name()).isEqualTo("NIT");
        assertThat(updated.active()).isTrue();
    }

    @Test
    void disableAndEnableDocumentType() {
        InMemoryDocumentTypeRepository repository = new InMemoryDocumentTypeRepository();
        repository.save(DocumentType.restore(13L, "Cedula", null, true));
        DocumentTypeManagementService service = new DocumentTypeManagementService(repository);

        service.disable(13L);
        assertThat(repository.findByCode(13L).orElseThrow().active()).isFalse();

        service.enable(13L);
        assertThat(repository.findByCode(13L).orElseThrow().active()).isTrue();
    }

    @Test
    void findByCodeRejectsMissingDocumentType() {
        DocumentTypeManagementService service = new DocumentTypeManagementService(new InMemoryDocumentTypeRepository());

        assertThatThrownBy(() -> service.findByCode(99L))
                .isInstanceOf(TipoDocumentoNotFoundException.class);
    }

    private static final class InMemoryDocumentTypeRepository implements DocumentTypeRepositoryPort {

        private final Map<Long, DocumentType> documentTypes = new LinkedHashMap<>();

        @Override
        public List<DocumentType> findAll() {
            return List.copyOf(documentTypes.values());
        }

        @Override
        public List<DocumentType> findActive() {
            return documentTypes.values().stream().filter(DocumentType::active).toList();
        }

        @Override
        public List<DocumentType> findInactive() {
            return documentTypes.values().stream().filter(documentType -> !documentType.active()).toList();
        }

        @Override
        public Optional<DocumentType> findByCode(Long code) {
            return Optional.ofNullable(documentTypes.get(code));
        }

        @Override
        public DocumentType save(DocumentType documentType) {
            documentTypes.put(documentType.code(), documentType);
            return documentType;
        }
    }
}
