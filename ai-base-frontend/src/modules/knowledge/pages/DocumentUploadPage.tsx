import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Button, Upload, message } from 'antd';
import type { UploadProps } from 'antd';
import { InboxOutlined } from '@ant-design/icons';

const { Dragger } = Upload;

export default function DocumentUploadPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [uploading, setUploading] = useState(false);

  const props: UploadProps = {
    name: 'file',
    multiple: true,
    beforeUpload: () => false,
    onChange(info) {
      const { file } = info;
      if (file.status === 'done') {
        message.success(`${file.name} 上传成功`);
      } else if (file.status === 'error') {
        message.error(`${file.name} 上传失败`);
      }
    },
    onDrop() {
      setUploading(true);
      setTimeout(() => setUploading(false), 1500);
    },
  };

  return (
    <div style={{ padding: '0 20px 20px' }}>
      <Button type="link" onClick={() => navigate(`/knowledge/${id}`)} style={{ padding: 0, marginBottom: 16 }}>
        ← 返回知识库详情
      </Button>

      <Card styles={{ body: { padding: 20 } }}>
        <h3 style={{ margin: '0 0 16px', fontSize: 16, fontWeight: 700 }}>上传文档</h3>
        <Dragger {...props} disabled={uploading} style={{ padding: '40px 0' }}>
          <p className="ant-upload-drag-icon">
            <InboxOutlined style={{ fontSize: 40, color: '#597ef7' }} />
          </p>
          <p className="ant-upload-text" style={{ fontSize: 14, fontWeight: 500 }}>
            点击或拖拽文件到此区域上传
          </p>
          <p className="ant-upload-hint" style={{ fontSize: 12, color: '#999' }}>
            支持 PDF、DOCX、TXT、Markdown 格式，单文件最大 50MB
          </p>
        </Dragger>

        <div style={{ marginTop: 16, padding: '12px 16px', background: '#fffcf0', borderRadius: 8, fontSize: 12, color: '#d97706' }}>
          上传后系统将自动对文档进行分段和向量化处理，处理完成后可在知识库详情页查看。
        </div>
      </Card>
    </div>
  );
}
