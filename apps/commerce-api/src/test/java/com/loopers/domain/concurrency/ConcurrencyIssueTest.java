package com.loopers.domain.concurrency;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.infrastructure.product.ProductEntity;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 동시성 문제를 재현하는 테스트
 * read-modify-write 패턴에서 발생하는 Lost Update 문제를 검증합니다.
 */
@SpringBootTest
class ConcurrencyIssueTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Long brandId;
    private Long productId;

    @BeforeEach
    void setUp() {
        // 브랜드 생성
        Brand brand = brandService.create("테스트 브랜드", "테스트 설명");
        brandId = brand.getId();

        // 상품 생성 (좋아요 0, 재고 10)
        Product product = Product.create(null, "테스트 상품", brandId, 10000, 10, 0);
        ProductEntity productEntity = ProductEntity.create(product);
        productId = productJpaRepository.save(productEntity).getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("좋아요 카운터 동시성 테스트")
    class LikeCountConcurrencyTest {

        @Test
        @DisplayName("동시에 100명이 좋아요를 눌러도 원자적 UPDATE로 정확한 카운트 유지")
        void concurrency_atomic_update_test() throws InterruptedException {
            // Given
            int threadCount = 100; // 스레드 수를 더 줄여서 더 현실적인 결과 얻기
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1); // 모든 스레드가 동시에 시작하도록
            CountDownLatch finishLatch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // When
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        // 모든 스레드가 동시에 시작하도록 대기
                        startLatch.await();
                        productService.increaseLikeCount(productId);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        e.printStackTrace();
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }
            
            // 모든 스레드가 준비될 때까지 충분히 대기
            Thread.sleep(200);
            // 동시에 시작
            startLatch.countDown();
            finishLatch.await();
            executor.shutdown();

            // Then
            Product product = productService.getById(productId);
            System.out.println("성공: " + successCount.get() + ", 실패: " + failureCount.get());
            System.out.println("예상: " + threadCount + ", 실제: " + product.getLikeCount());
            // 원자적 UPDATE로 인해 정확한 카운트 유지
            assertThat(product.getLikeCount()).isEqualTo(threadCount);
        }
    }
}
