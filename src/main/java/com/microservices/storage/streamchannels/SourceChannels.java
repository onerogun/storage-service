package com.microservices.storage.streamchannels;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface SourceChannels {

        String OUTPUT_FILE_CREATED_LOCATION = "output_file_created_location";
        String OUTPUT_FILE_DELETED_LOCATION = "output_file_deleted_location";
        String INPUT_ITEM_DELETED_FILE_LOCATION = "input_item_deleted_file_location";

        @Output("output_file_created_location")
        MessageChannel outputFileCreated();

        @Output("output_file_deleted_location")
        MessageChannel outputFileDeleted();

        @Input("input_item_deleted_file_location")
        SubscribableChannel inputItemDeleted();
}
