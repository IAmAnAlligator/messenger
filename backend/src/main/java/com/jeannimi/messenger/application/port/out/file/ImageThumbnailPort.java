package com.jeannimi.messenger.application.port.out.file;

import java.io.InputStream;

public interface ImageThumbnailPort {

  GeneratedThumbnail generate(InputStream original);
}
