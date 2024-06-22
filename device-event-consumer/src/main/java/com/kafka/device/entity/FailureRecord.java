package com.kafka.device.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FailureRecord {

    @Id
    @GeneratedValue
    private Integer id;

    private String topic; //string topic just to represent from which topic this failed record came from.

    private Integer keyValue;

    private String errorRecord;

    private Integer partition; //going to be the partition where this message comes from.

    private Long offset_value; //Going to be adding a offset value to because these are all mandatory in order to construct a consumer

    private String exception;

    private String status; //we are going to control whether the record is a retrievable record or whether it is a dead record.

}
