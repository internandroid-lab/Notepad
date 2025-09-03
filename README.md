# Notepad App

Ứng dụng ghi chú đơn giản được xây dựng bằng Kotlin với các tính năng:

## Tính năng chính

### Màn hình Home

- **Top Bar**: Hiển thị tiêu đề "Note", biểu tượng tìm kiếm và sắp xếp
- **Tìm kiếm**: Nhấn biểu tượng tìm kiếm để hiển thị thanh tìm kiếm
- **Sắp xếp**: Nhấn biểu tượng sắp xếp để chọn kiểu sắp xếp (theo ngày hoặc tiêu đề)
- **Danh sách ghi chú**: Hiển thị tât cả ghi chú với tiêu đề và thời gian chỉnh sửa cuối
- **FloatingActionButton**: Nhấn để tạo ghi chú mới

### Màn hình Edit Note

- **Top Bar**: Nút quay lại, tiêu đề "Note", nút SAVE và UNDO
- **Chỉnh sửa**: Có thể chỉnh sửa tiêu đề và nội dung ghi chú
- **Lưu**: Kiểm tra nội dung và lưu vào database
- **Undo**: Xóa ký tự cuối trong nội dung

## Công nghệ sử dụng

- **Kotlin**: Ngôn ngữ lập trình chính
- **XML Layout**: Thiết kế giao diện
- **Navigation Component**: Điều hướng giữa các màn hình
- **Room Database**: Lưu trữ dữ liệu local
- **MVVM Pattern**: Kiến trúc ViewModel và LiveData
- **RecyclerView**: Hiển thị danh sách ghi chú

## Cấu trúc dự án

```
app/src/main/java/com/example/notepad/
├── db/
│   ├── Note.kt              # Entity class
│   ├── NoteDao.kt           # Data Access Object
│   ├── Converters.kt        # Type converters
│   └── AppDatabase.kt       # Room database
├── fragment/
│   ├── HomeFragment.kt      # Màn hình chính
│   └── EditNoteFragment.kt  # Màn hình chỉnh sửa
├── viewmodel/
│   ├── HomeViewModel.kt     # ViewModel cho Home
│   ├── EditNoteViewModel.kt # ViewModel cho EditNote
│   └── *ViewModelFactory.kt # Factory classes
├── adapter/
│   └── NoteAdapter.kt       # RecyclerView adapter
├── repository/
│   └── NoteRepository.kt    # Data repository
├── MainActivity.kt          # Activity chính
└── NotepadApplication.kt    # Application class
```

## Hướng dẫn chạy

1. Mở dự án trong Android Studio
2. Sync Gradle files
3. Chạy ứng dụng trên device/emulator

## Ghi chú

- App tự động tạo một số ghi chú mẫu khi lần đầu khởi chạy
- Database được lưu trữ local và persistent
- Hỗ trợ tìm kiếm theo tiêu đề và nội dung
- Hỗ trợ sắp xếp theo ngày và tiêu đề