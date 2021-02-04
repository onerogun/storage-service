package com.microservices.storage.streamchannels;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface SourceChannels {

        String OUTPUT_FILE_CREATED_LOCATION = "output_file_created_location";
        String OUTPUT_FILE_DELETED_LOCATION = "output_file_deleted_location";

        @Output("output_file_created_location")
        MessageChannel outputFileCreated();

        @Output("output_file_deleted_location")
        MessageChannel outputFileDeleted();
}
