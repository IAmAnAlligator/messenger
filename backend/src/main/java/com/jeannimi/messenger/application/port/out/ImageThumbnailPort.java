package com.jeannimi.messenger.application.port.out;

import java.io.InputStream;

public interface ImageThumbnailPort {

  GeneratedThumbnail generate(InputStream original);
}
