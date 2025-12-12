package com.subcycle.service;

import com.subcycle.dto.SubscriptionRequest;
import com.subcycle.dto.SubscriptionResponse;
import com.subcycle.entity.Category;
import com.subcycle.entity.Subscription;
import com.subcycle.entity.User;
import com.subcycle.repository.CategoryRepository;
import com.subcycle.repository.SubscriptionRepository;
import com.subcycle.specification.SubscriptionSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User testUser;
    private Category testCategory;
    private Subscription testSubscription;
    private SubscriptionRequest subscriptionRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setUser(testUser);
        testCategory.setName("串流影音");
        testCategory.setIcon("play-circle");
        testCategory.setColor("#EF4444");

        testSubscription = new Subscription();
        testSubscription.setId(1L);
        testSubscription.setUser(testUser);
        testSubscription.setName("Netflix");
        testSubscription.setPrice(new BigDecimal("390"));
        testSubscription.setBillingCycle("monthly");
        testSubscription.setNextPaymentDate(LocalDate.now().plusMonths(1));
        testSubscription.setStatus("active");
        testSubscription.setCategory(testCategory);

        subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setName("Netflix");
        subscriptionRequest.setAmount(new BigDecimal("390"));
        subscriptionRequest.setCycle("monthly");
        subscriptionRequest.setNextPaymentDate(LocalDate.now().plusMonths(1));
        subscriptionRequest.setStatus("active");
        subscriptionRequest.setCategoryId(1L);
    }

    @Test
    void testGetSubscriptions_Success() {
        // Arrange
        List<Subscription> subscriptions = Arrays.asList(testSubscription);
        when(subscriptionRepository.findByUserOrderByNextPaymentDateAsc(testUser))
                .thenReturn(subscriptions);

        // Act
        List<SubscriptionResponse> result = subscriptionService.getSubscriptions(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Netflix", result.get(0).getName());
        assertEquals(new BigDecimal("390"), result.get(0).getAmount());
        verify(subscriptionRepository).findByUserOrderByNextPaymentDateAsc(testUser);
    }

    @Test
    void testGetSubscriptionsWithPagination_Success() {
        // Arrange
        List<Subscription> subscriptions = Arrays.asList(testSubscription);
        Page<Subscription> page = new PageImpl<>(subscriptions);

        when(subscriptionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        Page<SubscriptionResponse> result = subscriptionService.getSubscriptionsWithPagination(
                testUser, "Netflix", "active", 1L, "monthly", 0, 10, "nextPaymentDate", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Netflix", result.getContent().get(0).getName());
        verify(subscriptionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testCreateSubscription_Success() {
        // Arrange
        when(categoryRepository.findByIdAndUser(1L, testUser))
                .thenReturn(Optional.of(testCategory));
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(testSubscription);

        // Act
        SubscriptionResponse result = subscriptionService.createSubscription(testUser, subscriptionRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Netflix", result.getName());
        assertEquals(new BigDecimal("390"), result.getAmount());
        assertEquals("monthly", result.getCycle());
        verify(categoryRepository).findByIdAndUser(1L, testUser);
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testCreateSubscription_CategoryNotFound() {
        // Arrange
        when(categoryRepository.findByIdAndUser(1L, testUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            subscriptionService.createSubscription(testUser, subscriptionRequest);
        });

        verify(categoryRepository).findByIdAndUser(1L, testUser);
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void testUpdateSubscription_Success() {
        // Arrange
        when(subscriptionRepository.findByIdAndUser(1L, testUser))
                .thenReturn(Optional.of(testSubscription));
        when(categoryRepository.findByIdAndUser(1L, testUser))
                .thenReturn(Optional.of(testCategory));
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(testSubscription);

        SubscriptionRequest updateRequest = new SubscriptionRequest();
        updateRequest.setName("Netflix Premium");
        updateRequest.setAmount(new BigDecimal("490"));
        updateRequest.setCycle("monthly");
        updateRequest.setNextPaymentDate(LocalDate.now().plusMonths(1));
        updateRequest.setStatus("active");
        updateRequest.setCategoryId(1L);

        // Act
        SubscriptionResponse result = subscriptionService.updateSubscription(testUser, 1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(subscriptionRepository).findByIdAndUser(1L, testUser);
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testUpdateSubscription_NotFound() {
        // Arrange
        when(subscriptionRepository.findByIdAndUser(999L, testUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            subscriptionService.updateSubscription(testUser, 999L, subscriptionRequest);
        });

        verify(subscriptionRepository).findByIdAndUser(999L, testUser);
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void testDeleteSubscription_Success() {
        // Arrange
        when(subscriptionRepository.findByIdAndUser(1L, testUser))
                .thenReturn(Optional.of(testSubscription));
        doNothing().when(subscriptionRepository).delete(testSubscription);

        // Act
        subscriptionService.deleteSubscription(testUser, 1L);

        // Assert
        verify(subscriptionRepository).findByIdAndUser(1L, testUser);
        verify(subscriptionRepository).delete(testSubscription);
    }

    @Test
    void testDeleteSubscription_NotFound() {
        // Arrange
        when(subscriptionRepository.findByIdAndUser(999L, testUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            subscriptionService.deleteSubscription(testUser, 999L);
        });

        verify(subscriptionRepository).findByIdAndUser(999L, testUser);
        verify(subscriptionRepository, never()).delete(any(Subscription.class));
    }
}
