SK MALL
# SK MALL

클라우드 네이티브 애플리케이션의 개발에 요구되는 체크포인트 확인
- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW

# Table of contents

- [SK MALL](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [ConfigMap 설정](#ConfigMap-설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
    - [Self healing](#Liveness-Probe)

# 서비스 시나리오

SK MALL 서비스

기능적 요구사항
1. 고객이 상품을 선택한다.
2. 창고에 재고를 확인 후 재고가 있으면 주문한다.
3. 주문이 성공하면 배송을 시작한다.
4. 배송이 시작되면 주문 상태를 업데이트 하고 창고의 재고를 주문수량만큼 감소시킨다.
6. 고객이 주문을 취소 한다.
7. 주문이 추소되면 배송을 취소, 주문 상태를 업데이트하고, 창고의 재고를 주문취소수량만큼 증가시킨다.

비기능적 요구사항
1. 트랜잭션
    1. 창고의 재고가 확인되지 않은 상품은 주문할 수 없다.  Sync 호출 
2. 장애격리
    1. 배송 기능이 수행되지 않더라도 상품 주문, 주문 취소를 을 받을 수 있다. Async (event-driven), Eventual Consistency
    1. 주문이 과중되면 주문을 잠시동안 받지 않고 주문 요청을 잠시후에 하도록 유도한다  Circuit breaker, fallback
3. 성능
    1. 주문상태 대한 정보 한 화면에서 확인 할 수 있다. CQRS

# 체크포인트

- 분석 설계

  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?

# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)
![asis](https://user-images.githubusercontent.com/90441340/132832765-2ee6cd26-2841-43cd-b9ab-666664ee2de1.jpg)

## TO-BE 조직 (Vertically-Aligned)
![1](https://user-images.githubusercontent.com/90441340/135399124-0d8d1478-2dec-41e1-b8fc-d954f6e810de.jpg)

## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과 : https://www.msaez.io/#/storming/gwUip42y6gYTDeNEXyvpVgHKs7p2/acf0b090cf4293c3124c5a79baca7dbc

### 이벤트 도출
![2](https://user-images.githubusercontent.com/90441340/135399840-9212187e-f937-4c21-8bac-65dbe3f85ad1.jpg)

### 부적격 이벤트 탈락
![3](https://user-images.githubusercontent.com/90441340/135399849-4cc89441-c294-4cd6-b168-6e360cef71e3.jpg)

    - 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
        - 주문 시 > ProductRegistered : 상품등록이 완료되어야 주문 이벤트가 발생하는 ACID 트랜잭션을 적용이 필요하므로 stockIncreased, stockReduced와 통합하여 처리

### 완성된 1차 모형!
![image](https://user-images.githubusercontent.com/90441340/135401001-5b1f5da4-385e-4536-927d-38e185fcde90.png)
 
 - View Model 추가

### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증
![image](https://user-images.githubusercontent.com/90441340/135401395-8f84b71a-8173-4c16-aeaf-59eb880be011.png)

    - 고객이 상품을 선택하고 주문을 한다.(ok)
    - 상품재고를 확인한다. (ok)
    - 주문이 완료되면 배송관리시스템에 전달된다.(ok)
    - 배송이 시작되면 창과관리시스템에서 재고를 조정한다. (ok)
    - 고객은 중간중간 주문 현황을 조회한다. (View-green sticker 의 추가로 ok)

![image](https://user-images.githubusercontent.com/90441340/135401474-4a7d6ce1-7eff-4262-9104-0cbfbc598552.png)

    - 고객이 주문을 취소할 수 있다. (ok)
    - 주문이 취소되면 배송이 취소, 주문상태가 업데이트 되며, 재고가 수정된다. (ok)  

### 비기능 요구사항에 대한 검증
![image](https://user-images.githubusercontent.com/90441340/135401001-5b1f5da4-385e-4536-927d-38e185fcde90.png)

- 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
	- 주문 시 재고 확인 처리:  재고확인 완료되지 않은 주문은 절대 받지 않는다는 정책에 따라, ACID 트랜잭션 적용. 주문 처리와 재고확인에 대해서는 Request-Response 방식 처리
	- 주문 완료 시 배송 상태 및 재고 수량 변경 처리:  주문에서  마이크로서비스로 주문내역이 전달되는 과정에 있어서  delivery, warehouse 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.
	- 나머지 모든 inter-microservice 트랜잭션: 주문상태, 재고수량 등 모든 이벤트에 대해 MyPage처리 등, 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.
	- 배송 관리 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency
        - 주문시스템이 과중되면 사용자를 잠시동안 받지 않고 승인을 잠시후에 하도록 유도한다  Circuit breaker, fallback

## 헥사고날 아키텍처 다이어그램 도출
![image](https://user-images.githubusercontent.com/90441340/135404310-2f5d8d45-dde7-4c42-8c83-6856a1a25705.png)

    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐

## 구현:
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)
```
   cd order
   mvn spring-boot:run
   
   cd delivery
   mvn spring-boot:run
   
   cd warehouse
   mvn spring-boot:run
   
   cd mypage
   mvn spring-boot:run
   
   cd gateway
   mvn spring-boot:run
   
```


## CQRS

상품 주문/취소/매핑 등 총 Status 및 백신 종류 에 대하여 고객이 조회 할 수 있도록 CQRS 로 구현하였다.
- order, delivery, warehouse 개별 Aggregate Status 를 통합 조회하여 성능 Issue 를 사전에 예방할 수 있다.
- 비동기식으로 처리되어 발행된 이벤트 기반 Kafka 를 통해 수신/처리 되어 별도 Table 에 관리한다

- ("Ordered" 이벤트 발생 시, Pub/Sub 기반으로 별도 테이블에 저장)
![image](https://user-images.githubusercontent.com/90441340/135565931-61f8a290-ccd3-47b7-97db-72cb628b47c4.png)

- ("Shipped" 이벤트 발생 시, Pub/Sub 기반으로 별도 테이블에 저장)
![image](https://user-images.githubusercontent.com/90441340/135565963-4aa6be4e-38ef-4162-aaa6-21a532c29dd6.png)

- ("OrderCancelled" 이벤트 발생 시, Pub/Sub 기반으로 별도 테이블에 저장)
![image](https://user-images.githubusercontent.com/90441340/135565986-999af06e-4023-4e86-8ed8-46f683694d36.png)

- 실제로 view 페이지를 조회해 보면 모든 주문에 대한 정보, 배송 상태 등의 정보를 종합적으로 알 수 있다.
![image](https://user-images.githubusercontent.com/90441340/135565831-cfd8f183-1a38-48c6-bd48-01cebe4393bf.png)

## API 게이트웨이

 1. gateway 스프링부트 App을 추가 후 application.yaml내에 각 마이크로 서비스의 routes 를 추가하고 gateway 서버의 포트를 8080 으로 설정함
          - application.yaml 예시

       ![image](https://user-images.githubusercontent.com/90441340/135566030-2c073e8b-3695-4629-aff1-b6d864ff54e7.png)
       
## Correlation
skmall 프로젝트에서는 PolicyHandler에서 처리 시 어떤 건에 대한 처리인지를 구별하기 위한 Correlation-key 구현을 
이벤트 클래스 안의 변수로 전달받아 서비스간 연관된 처리를 정확하게 구현하고 있습니다. 

아래의 구현 예제를 보면

주문(order)을 하면 동시에 연관된 배송상태(delivery)의 서비스의 상태가 적당하게 변경이 되고,
주문을 취소를 수행하면 다시 연관된  배송상태(delivery), 재고(warehouse)의 서비스의 상태값 등의 데이터가 적당한 상태로 변경되는 것을
확인할 수 있습니다.

- 주문 전 재고 등록
http POST http://localhost:8081/warehouses productId=123 name=TV stock=100
![image](https://user-images.githubusercontent.com/90441340/135566650-86465c4c-3426-438f-8cd1-44e6fe74866b.png)

- 주문
http POST http://localhost:8082/orders customerId=111 productId=1 qty=1
![image](https://user-images.githubusercontent.com/90441340/135566814-db347e42-b76f-4905-b383-770997206b21.png)

- 주문 - 확인
"status": "주문성공" 확인
![image](https://user-images.githubusercontent.com/90441340/135566978-0ffbe233-ff01-4cf8-8ef7-7b26b16b6065.png)

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. (예시는 Order 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 현실에서 발생가는한 이벤트에 의하여 마이크로 서비스들이 상호 작용하기 좋은 모델링으로 구현을 하였다.

```
@Entity
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long productId;
    private Integer qty;
    private String status;
    private Long customerId;

    @PostPersist
    public void onPostPersist(){
        
        Ordered ordered = new Ordered();
        BeanUtils.copyProperties(this, ordered);
        ordered.publishAfterCommit();        

    }
    @PostRemove
    public void onPostRemove(){
        OrderCancelled orderCancelled = new OrderCancelled();
        BeanUtils.copyProperties(this, orderCancelled);
        orderCancelled.publishAfterCommit();

    }
    @PrePersist
    public void onPrePersist(){
    }
    @PreRemove
    public void onPreRemove(){
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

}
```

- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다

```
package skmall;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="orders", path="orders")
public interface OrderRepository extends PagingAndSortingRepository<Order, Long>{
}
```

- 적용 후 REST API 의 테스트

```
#order 서비스의 주문 요청
http POST http://localhost:8082/orders customerId=111 productId=1 qty=1

#order 서비스의 주문 취소 요청
http PATCH http://localhost:8082/orders/1 status="주문취소"

#order 서비스의 주문 상태 확인
http GET http://localhost:8082/orders

#warehouse 상품 및 
http GET http://localhost:8081/warehouse
```


## 동기식 호출(Sync) 과 Fallback 처리

분석단계에서의 조건 중 하나로 주문(order)-> 재고확인(warehouse) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 
호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient로 이용하여 호출하도록 한다.

- 승인 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
#WarehouseService.java

import java.util.Date;

@FeignClient(name="warehouse", url="${api.warehouse.url}")
public interface WarehouseService {
    @RequestMapping(method= RequestMethod.GET, path="/warehouses/{id}")
    public Warehouse getWarehouse(@PathVariable("id") Long id);

}
```

- 주문 요청을 받기 전(@PrePersist) 재고 확인을 동기(Sync)로 요청하도록 처리
![image](https://user-images.githubusercontent.com/90441340/135559136-76b36777-c869-42bd-8a73-ac41bd8a0896.png)
```
#Order.java

@PrePersist
public void onPrePersist(){

// Get request from Warehouse
skmall.external.Warehouse warehouse =
    OrderApplication.applicationContext.getBean(skmall.external.WarehouseService.class).getWarehouse(productId);

if(warehouse.getStock() > 0 ){
    Ordered ordered = new Ordered();
    ordered.setStatus("OrderSuccessed");
    BeanUtils.copyProperties(this, ordered);
    ordered.publishAfterCommit();
} 

}
    
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, warehouse 시스템이 장애가 나면 주문도 못받는다는 것을 확인

```
# warehouse 서비스를 잠시 내려놓음
```
```
# 주문 요청  - status:null 확인
http POST http://localhost:8082/orders customerId=111 productId=1 qty=1
```
![image](https://user-images.githubusercontent.com/90441340/135569218-7e710522-48bb-4fba-bfbd-3caf0937ba69.png)
```
# warehouse 서비스 재기동
cd warehouse
mvn spring-boot:run
```

```
# 주문 요청  - Success

http POST http://localhost:8082/orders customerId=111 productId=1 qty=1
```

![image](https://user-images.githubusercontent.com/90441340/135566978-0ffbe233-ff01-4cf8-8ef7-7b26b16b6065.png)


- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커 처리는 운영단계에서 설명한다.)

# 운영

## CI/CD 설정
각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 AWS를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하 buildspec.yml 에 포함되었다.

AWS CodeBuild 적용 현황
![image](https://user-images.githubusercontent.com/90441340/135569444-c362009e-7ca3-49fa-a916-e23e2587506a.png)

webhook을 통한 CI 확인
![image](https://user-images.githubusercontent.com/90441340/135569595-2895927e-a1b4-4b6f-ae1b-97dc057544ac.png)

AWS ECR 적용 현황
![image](https://user-images.githubusercontent.com/90441340/135569781-d8b7edc9-a3a2-4fa4-9981-9f1543eef9e0.png)

EKS에 배포된 내용
![image](https://user-images.githubusercontent.com/90441340/135573203-d3dd3be6-12f2-4bc7-bac7-0d220be1a102.png)

## ConfigMap 설정


 동기 호출 URL을 ConfigMap에 등록하여 사용


 kubectl apply -f configmap

```
 apiVersion: v1
 kind: ConfigMap
 metadata:
   name: skmall-configmap
   namespace: skmall
 data:
   apiurl: "http://user17-gateway:8080"

```
buildspec 수정

```
              spec:
                containers:
                  - name: $_PROJECT_NAME
                    image: $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$_PROJECT_NAME:$CODEBUILD_RESOLVED_SOURCE_VERSION
                    ports:
                      - containerPort: 8080
                    env:
                    - name: apiurl
                      valueFrom:
                        configMapKeyRef:
                          name: skmall-configmap
                          key: apiurl 
                        
```            
application.yml 수정
```
prop:
  aprv:
    url: ${apiurl}
``` 
