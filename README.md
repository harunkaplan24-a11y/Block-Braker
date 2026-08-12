# Online Tetris

Android için klasik Tetris oyununun başlangıç sürümü.

## Şu an hazır
- 10x20 Tetris alanı
- 7 klasik taş şekli
- Dokunmatik sağ/sol hareket
- Döndürme ve hızlı düşürme
- Satır temizleme
- Puan sistemi: 1/2/3/4 satır = 100/300/500/800 x level
- Her 10 satırda level artışı
- Level yükseldikçe düşüş hızının artması
- Oyun sonu ve yeniden başlatma

## Sonraki aşama: online
Online oyuncu adı, yüksek skor ve küresel sıralama için Firebase/Firestore bağlantısı eklenecek. Bu bağlantı için Firebase projesinin `google-services.json` dosyası gereklidir; gizli/kişisel anahtarlar GitHub'a eklenmeyecektir.

## Android Studio
Projeyi Android Studio ile açıp Gradle senkronizasyonundan sonra çalıştırabilirsiniz.
