# J5-TravelBookJava
## Map örneği projemde Room ve RxJava, RxAndroid kütüphanelerini kullandım
### Kütüphaneleri projemde nasıl kullandığımı açıklayayım:
### Room
> Verilerimi daha önceki projemde SQLite'da tutabilmek için SQLite sorgu ve yöntemlerini java kodlarımın arasında yazıyordum. Bu, kodları biraz savruk ve geliştirilmesi zor hale getiriyor.
> Bunun yerine local database'de daha kontrol edilebilir ve kulllanışlı olan room kütüphanesini kullandım.
> Room kütüphanesi bir varlık(entity),bir veri erişim nesnesi(dao) ve RoomDatabase tutuyor.

##### Entity
- Annotation (`@Entity`) gösterimi ile SQLite table oluşturuyoruz ve table prop'larını `@ColumnInfo` ile belirtiyoruz. `@PrimaryKey` sütun sırasını otomatik olarak arttırsın diye ekleniyor. Bunun dışında kullanılmayacağı için yapıcı metotta tanımlanmıyor.

![image](https://github.com/KyneticHaze/J5-TravelBookJava/blob/master/app/src/main/java/com/furkanharmanci/travelbookjava/readMeAssets/IntelliJ%20Snippet.png)

##### Dao (Data access object)
- Burada tablo işlemlerini ve sorgularını yapabilmek için method tanımlıyoruz. `@Query` sorgusunun içine ne istediğimizi tam olarak yazarak sorgu methodunu yazıyoruz. Diğer SQLite işlemleri için uygun annotation'ları da yazarak methodları tanımlıyoruz.
> Flowable, Completable RxJava araçlarını Rxjava tarafında açıklayacağım. 
  
![image](https://github.com/KyneticHaze/J5-TravelBookJava/blob/master/app/src/main/java/com/furkanharmanci/travelbookjava/readMeAssets/IntelliJ%20Snippet2.png)

##### Database 
- RoomDatabase sınıfından kalıtım alan soyut sınıfımıza `@Database` annotation'unu yerleştiriyoruz. Ve içine dalgalı parantez içerisinde model sınıfımızı sonuna (.class) şeklinde yazarak kapatıyoruz. Yanına version olarak varsayılan haliyle 1 yazabiliriz. Database'de değişiklik yapıldığında arttırmalar yapılmalı.
- İçerisine soyut şekilde Dao'muzu döndürecek bir method tanımlıyoruz.

![image](https://github.com/KyneticHaze/J5-TravelBookJava/blob/master/app/src/main/java/com/furkanharmanci/travelbookjava/readMeAssets/IntelliJ%20Snippet3.png)

##### Database'i ve SQLite işlevleri içeren soyut methodunu bir aktivite içerisinde inşa etme
- Soyut sınıfımızdan bir instance init ediyoruz öncelikle. Activite içerisinde şunu yazıyoruz:
```
PlaceDatabase db;
PlaceDao placeDao;
```
- Sonra Room sınıfının veritabanı inşa edici methoduna öncelikle context veriyoruz sonra veritabanımızı (.class) şeklinde yazıp veritabanımızın ismini vererek methodu bitiriyoruz. İnşa edilsin diye de sonuna `build()` methodunu yazarak bitiyoruz.
- Devamında işlevleri içeren PlaceDao'muzdan instance'ın içine `placeDao()` methodumuzu veriyoruz.

![image](https://github.com/KyneticHaze/J5-TravelBookJava/blob/master/app/src/main/java/com/furkanharmanci/travelbookjava/readMeAssets/IntelliJ%20Snippet4.png)

### RxJava
- Bu işlemi yaptığımızda arayüz katmanında kullanıcının deneyimini kısıtlayan durumlar ortaya çıkabilir. Bu sebepler rxJava ile işlem katmanı değişiklikleri yapıyoruz. Başka bir katmanda işlemi yapıp gözlemliyoruz.
- Bu işlemler Observable ile yapıldığı gibi Disposable ile kullan at şeklinde de yapabiliriz. Öncelikle Activite içine `private CompositeDisposable disposable = new CompositeDisposable();` şeklinde tanımlama ve atfetme işlemini yapıyoruz.
- Sonraki işlem aşağıdaki görselde anlatıldığı gibi:

![image](https://github.com/KyneticHaze/J5-TravelBookJava/blob/master/app/src/main/java/com/furkanharmanci/travelbookjava/readMeAssets/IntelliJ%20Snippet5.png)

* Yapılan işlemin cevabını ele alacağımız bir methot tanımladık ve onu subscribe içine referans şeklinde verdik.

![image](https://github.com/KyneticHaze/J5-TravelBookJava/blob/master/app/src/main/java/com/furkanharmanci/travelbookjava/readMeAssets/IntelliJ%20Snippet6.png)

### Bu kadar! 🥳
#### Biraz uzun oldu ama anlaşılır olduğunu düşünüyorum 😄
#### Sonraki örnek projede görüşmek üzere!
![](https://github.com/KyneticHaze/J5-TravelBookJava/blob/master/app/src/main/java/com/furkanharmanci/travelbookjava/readMeAssets/N1w2.gif)
