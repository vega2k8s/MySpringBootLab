package com.rookies3.myspringbootlab.service;

import com.rookies3.myspringbootlab.controller.dto.PublisherDTO;
import com.rookies3.myspringbootlab.entity.Publisher;
import com.rookies3.myspringbootlab.exception.BusinessException;
import com.rookies3.myspringbootlab.exception.ErrorCode;
import com.rookies3.myspringbootlab.repository.BookRepository;
import com.rookies3.myspringbootlab.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Publisher 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 출판사의 CRUD 작업, 유효성 검증, 도서 수 계산 등을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;

    /**
     * 모든 출판사를 조회합니다.
     * 각 출판사의 도서 수를 별도로 계산하여 포함시킵니다.
     * Publisher.books 컬렉션에 접근하지 않아 N+1 문제를 방지합니다.
     *
     * @return 모든 출판사의 간단한 정보 DTO 목록 (도서 수 포함)
     */
    public List<PublisherDTO.SimpleResponse> getAllPublishers() {
        List<Publisher> publishers = publisherRepository.findAll();
        
        return publishers.stream()
                .map(publisher -> {
                    Long bookCount = bookRepository.countByPublisherId(publisher.getId());
                    return PublisherDTO.SimpleResponse.fromEntityWithCount(publisher, bookCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * ID로 특정 출판사를 조회합니다.
     * 해당 출판사의 모든 도서 정보를 포함하여 반환합니다.
     * Fetch Join을 사용하여 한 번의 쿼리로 출판사와 도서를 모두 조회합니다.
     *
     * @param id 조회할 출판사의 ID
     * @return 출판사의 상세 정보 DTO (도서 목록 포함)
     * @throws BusinessException 출판사를 찾을 수 없는 경우
     */
    public PublisherDTO.Response getPublisherById(Long id) {
        Publisher publisher = publisherRepository.findByIdWithBooks(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Publisher", "id", id));
        return PublisherDTO.Response.fromEntity(publisher);
    }

    /**
     * 이름으로 특정 출판사를 조회합니다.
     * 출판사 이름은 고유하므로 단일 결과를 반환합니다.
     *
     * @param name 조회할 출판사의 이름
     * @return 출판사의 상세 정보 DTO
     * @throws BusinessException 출판사를 찾을 수 없는 경우
     */
    public PublisherDTO.Response getPublisherByName(String name) {
        Publisher publisher = publisherRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Publisher", "name", name));
        return PublisherDTO.Response.fromEntity(publisher);
    }

    /**
     * 새로운 출판사를 생성합니다.
     * 출판사 이름의 중복을 검사한 후 생성합니다.
     *
     * @param request 출판사 생성 요청 DTO
     * @return 생성된 출판사 응답 DTO
     * @throws BusinessException 출판사 이름이 중복된 경우
     */
    @Transactional
    public PublisherDTO.Response createPublisher(PublisherDTO.Request request) {
        // Validate publisher name is not already in use
        if (publisherRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.PUBLISHER_NAME_DUPLICATE,
                    request.getName());
        }

        // Create publisher entity
        Publisher publisher = Publisher.builder()
                .name(request.getName())
                .establishedDate(request.getEstablishedDate())
                .address(request.getAddress())
                .build();

        // Save and return the publisher
        Publisher savedPublisher = publisherRepository.save(publisher);
        return PublisherDTO.Response.fromEntity(savedPublisher);
    }

    @Transactional
    public PublisherDTO.Response updatePublisher(Long id, PublisherDTO.Request request) {
        // Find the publisher
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Publisher", "id", id));

        // Check if another publisher already has the name
        if (!publisher.getName().equals(request.getName()) &&
                publisherRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.PUBLISHER_NAME_DUPLICATE,
                    request.getName());
        }

        // Update publisher info
        publisher.setName(request.getName());
        publisher.setEstablishedDate(request.getEstablishedDate());
        publisher.setAddress(request.getAddress());

        // Save and return updated publisher
        Publisher updatedPublisher = publisherRepository.save(publisher);
        return PublisherDTO.Response.fromEntity(updatedPublisher);
    }

    @Transactional
    public void deletePublisher(Long id) {
        if (!publisherRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Publisher", "id", id);
        }

        // Check if publisher has books
        Long bookCount = bookRepository.countByPublisherId(id);
        if (bookCount > 0) {
            throw new BusinessException(ErrorCode.PUBLISHER_HAS_BOOKS,
                    id, bookCount);
        }

        publisherRepository.deleteById(id);
    }
}