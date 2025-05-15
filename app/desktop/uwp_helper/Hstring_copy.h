typedef void* HSTRING;

typedef HSTRING* HSTRING_BUFFER;

typedef struct _HSTRING_HEADER {
    void* reserved1;
    union {
        void* reserved2;
        char reserved3[24];
    };
} HSTRING_HEADER;
