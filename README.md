# trading-api
Backend Case Study


Kurulum için öncelikle Java, Maven kurulumlarının tamamlanması gerekmektedir. Uygulama Spring Boot framework üzerinde çalışmaktadır. Dolayısı ile IDE yapılandırması bu şekilde ayarlanmalıdır.<br />

src -> main -> java -> resources paketinde <br />
-application.properties dosyasında;<br />

spring.datasource.url=jdbc:postgresql://localhost:5432/trading   -----> bu adreste trading adında bir db olmaz ise uygulama doğru çalışmayacaktır !! Tablolarınız ORM tarafıdndan db'ye insert edilecektir.<br />
spring.datasource.username=postgres -----> postgresql_username<br />
spring.datasource.password=1234 ------> postgresql_password
<br />

pom.xml dosyası üzerinde maven kullanarak gerekli bağımlılıkların kurulması gerekmektedir. <br />
Bunun için IDE üzerinden clean->install->compile komutlarını çalıştırarak maven projesini derleyebilirsiniz.<br />

Proje Spring Boot projesi olarak;<br />
target class = src->main->java->TradingApiApplication <br />
olarak belirlenmelidir.<br />


