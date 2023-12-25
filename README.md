# trading-api
Backend Case Study


Kurulum için öncelikle Java, Maven kurulumlarının tamamlanması gerekmektedir. Uygulama Spring Boot framework üzerinde çalışmaktadır. Dolayısı ile IDE yapılandırması bu şekilde ayarlanmalıdır. 

src -> main -> java -> resources paketinde 
-application.properties dosyasında;

spring.datasource.url=jdbc:postgresql://localhost:5432/trading   -----> bu adreste trading adında bir db olmaz ise uygulama doğru çalışmayacaktır!!!
spring.datasource.username=postgres -----> postgresql_username
spring.datasource.password=1234 ------> postgresql_password


pom.xml dosyası üzerinde maven kullanarak gerekli bağımlılıkların kurulması gerekmektedir. 
Bunun için IDE üzerinden clean->install->compile komutlarını çalıştırarak maven projesini derleyebilirsiniz.

Proje Spring Boot projesi olarak;
target class = src->main->java->TradingApiApplication 
olarak belirlenmelidir.


