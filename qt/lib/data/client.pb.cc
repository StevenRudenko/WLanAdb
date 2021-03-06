// Generated by the protocol buffer compiler.  DO NOT EDIT!

#define INTERNAL_SUPPRESS_PROTOBUF_FIELD_DEPRECATION
#include "client.pb.h"

#include <algorithm>

#include <google/protobuf/stubs/once.h>
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/wire_format_lite_inl.h>
// @@protoc_insertion_point(includes)

namespace com {
namespace wlanadb {
namespace data {

void protobuf_ShutdownFile_client_2eproto() {
  delete Client::default_instance_;
}

void protobuf_AddDesc_client_2eproto() {
  static bool already_here = false;
  if (already_here) return;
  already_here = true;
  GOOGLE_PROTOBUF_VERIFY_VERSION;

  Client::default_instance_ = new Client();
  Client::default_instance_->InitAsDefaultInstance();
  ::google::protobuf::internal::OnShutdown(&protobuf_ShutdownFile_client_2eproto);
}

// Force AddDescriptors() to be called at static initialization time.
struct StaticDescriptorInitializer_client_2eproto {
  StaticDescriptorInitializer_client_2eproto() {
    protobuf_AddDesc_client_2eproto();
  }
} static_descriptor_initializer_client_2eproto_;


// ===================================================================

#ifndef _MSC_VER
const int Client::kIdFieldNumber;
const int Client::kIpFieldNumber;
const int Client::kPortFieldNumber;
const int Client::kNameFieldNumber;
const int Client::kModelFieldNumber;
const int Client::kFirmwareFieldNumber;
const int Client::kUsePinFieldNumber;
#endif  // !_MSC_VER

Client::Client()
  : ::google::protobuf::MessageLite() {
  SharedCtor();
}

void Client::InitAsDefaultInstance() {
}

Client::Client(const Client& from)
  : ::google::protobuf::MessageLite() {
  SharedCtor();
  MergeFrom(from);
}

void Client::SharedCtor() {
  _cached_size_ = 0;
  id_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  ip_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  port_ = 0;
  name_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  model_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  firmware_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  use_pin_ = false;
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
}

Client::~Client() {
  SharedDtor();
}

void Client::SharedDtor() {
  if (id_ != &::google::protobuf::internal::kEmptyString) {
    delete id_;
  }
  if (ip_ != &::google::protobuf::internal::kEmptyString) {
    delete ip_;
  }
  if (name_ != &::google::protobuf::internal::kEmptyString) {
    delete name_;
  }
  if (model_ != &::google::protobuf::internal::kEmptyString) {
    delete model_;
  }
  if (firmware_ != &::google::protobuf::internal::kEmptyString) {
    delete firmware_;
  }
  if (this != default_instance_) {
  }
}

void Client::SetCachedSize(int size) const {
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
}
const Client& Client::default_instance() {
  if (default_instance_ == NULL) protobuf_AddDesc_client_2eproto();  return *default_instance_;
}

Client* Client::default_instance_ = NULL;

Client* Client::New() const {
  return new Client;
}

void Client::Clear() {
  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    if (has_id()) {
      if (id_ != &::google::protobuf::internal::kEmptyString) {
        id_->clear();
      }
    }
    if (has_ip()) {
      if (ip_ != &::google::protobuf::internal::kEmptyString) {
        ip_->clear();
      }
    }
    port_ = 0;
    if (has_name()) {
      if (name_ != &::google::protobuf::internal::kEmptyString) {
        name_->clear();
      }
    }
    if (has_model()) {
      if (model_ != &::google::protobuf::internal::kEmptyString) {
        model_->clear();
      }
    }
    if (has_firmware()) {
      if (firmware_ != &::google::protobuf::internal::kEmptyString) {
        firmware_->clear();
      }
    }
    use_pin_ = false;
  }
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
}

bool Client::MergePartialFromCodedStream(
    ::google::protobuf::io::CodedInputStream* input) {
#define DO_(EXPRESSION) if (!(EXPRESSION)) return false
  ::google::protobuf::uint32 tag;
  while ((tag = input->ReadTag()) != 0) {
    switch (::google::protobuf::internal::WireFormatLite::GetTagFieldNumber(tag)) {
      // required string id = 1;
      case 1: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_id()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(18)) goto parse_ip;
        break;
      }
      
      // optional string ip = 2;
      case 2: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_ip:
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_ip()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(24)) goto parse_port;
        break;
      }
      
      // optional int32 port = 3;
      case 3: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_VARINT) {
         parse_port:
          DO_((::google::protobuf::internal::WireFormatLite::ReadPrimitive<
                   ::google::protobuf::int32, ::google::protobuf::internal::WireFormatLite::TYPE_INT32>(
                 input, &port_)));
          set_has_port();
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(34)) goto parse_name;
        break;
      }
      
      // optional string name = 4;
      case 4: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_name:
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_name()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(42)) goto parse_model;
        break;
      }
      
      // optional string model = 5;
      case 5: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_model:
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_model()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(50)) goto parse_firmware;
        break;
      }
      
      // optional string firmware = 6;
      case 6: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_firmware:
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_firmware()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(56)) goto parse_use_pin;
        break;
      }
      
      // optional bool use_pin = 7;
      case 7: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_VARINT) {
         parse_use_pin:
          DO_((::google::protobuf::internal::WireFormatLite::ReadPrimitive<
                   bool, ::google::protobuf::internal::WireFormatLite::TYPE_BOOL>(
                 input, &use_pin_)));
          set_has_use_pin();
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectAtEnd()) return true;
        break;
      }
      
      default: {
      handle_uninterpreted:
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_END_GROUP) {
          return true;
        }
        DO_(::google::protobuf::internal::WireFormatLite::SkipField(input, tag));
        break;
      }
    }
  }
  return true;
#undef DO_
}

void Client::SerializeWithCachedSizes(
    ::google::protobuf::io::CodedOutputStream* output) const {
  // required string id = 1;
  if (has_id()) {
    ::google::protobuf::internal::WireFormatLite::WriteString(
      1, this->id(), output);
  }
  
  // optional string ip = 2;
  if (has_ip()) {
    ::google::protobuf::internal::WireFormatLite::WriteString(
      2, this->ip(), output);
  }
  
  // optional int32 port = 3;
  if (has_port()) {
    ::google::protobuf::internal::WireFormatLite::WriteInt32(3, this->port(), output);
  }
  
  // optional string name = 4;
  if (has_name()) {
    ::google::protobuf::internal::WireFormatLite::WriteString(
      4, this->name(), output);
  }
  
  // optional string model = 5;
  if (has_model()) {
    ::google::protobuf::internal::WireFormatLite::WriteString(
      5, this->model(), output);
  }
  
  // optional string firmware = 6;
  if (has_firmware()) {
    ::google::protobuf::internal::WireFormatLite::WriteString(
      6, this->firmware(), output);
  }
  
  // optional bool use_pin = 7;
  if (has_use_pin()) {
    ::google::protobuf::internal::WireFormatLite::WriteBool(7, this->use_pin(), output);
  }
  
}

int Client::ByteSize() const {
  int total_size = 0;
  
  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    // required string id = 1;
    if (has_id()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->id());
    }
    
    // optional string ip = 2;
    if (has_ip()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->ip());
    }
    
    // optional int32 port = 3;
    if (has_port()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::Int32Size(
          this->port());
    }
    
    // optional string name = 4;
    if (has_name()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->name());
    }
    
    // optional string model = 5;
    if (has_model()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->model());
    }
    
    // optional string firmware = 6;
    if (has_firmware()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->firmware());
    }
    
    // optional bool use_pin = 7;
    if (has_use_pin()) {
      total_size += 1 + 1;
    }
    
  }
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = total_size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
  return total_size;
}

void Client::CheckTypeAndMergeFrom(
    const ::google::protobuf::MessageLite& from) {
  MergeFrom(*::google::protobuf::down_cast<const Client*>(&from));
}

void Client::MergeFrom(const Client& from) {
  GOOGLE_CHECK_NE(&from, this);
  if (from._has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    if (from.has_id()) {
      set_id(from.id());
    }
    if (from.has_ip()) {
      set_ip(from.ip());
    }
    if (from.has_port()) {
      set_port(from.port());
    }
    if (from.has_name()) {
      set_name(from.name());
    }
    if (from.has_model()) {
      set_model(from.model());
    }
    if (from.has_firmware()) {
      set_firmware(from.firmware());
    }
    if (from.has_use_pin()) {
      set_use_pin(from.use_pin());
    }
  }
}

void Client::CopyFrom(const Client& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

bool Client::IsInitialized() const {
  if ((_has_bits_[0] & 0x00000001) != 0x00000001) return false;
  
  return true;
}

void Client::Swap(Client* other) {
  if (other != this) {
    std::swap(id_, other->id_);
    std::swap(ip_, other->ip_);
    std::swap(port_, other->port_);
    std::swap(name_, other->name_);
    std::swap(model_, other->model_);
    std::swap(firmware_, other->firmware_);
    std::swap(use_pin_, other->use_pin_);
    std::swap(_has_bits_[0], other->_has_bits_[0]);
    std::swap(_cached_size_, other->_cached_size_);
  }
}

::std::string Client::GetTypeName() const {
  return "com.wlanadb.data.Client";
}


// @@protoc_insertion_point(namespace_scope)

}  // namespace data
}  // namespace wlanadb
}  // namespace com

// @@protoc_insertion_point(global_scope)
