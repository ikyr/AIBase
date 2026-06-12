import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Button, Upload, message } from 'antd';
import type { UploadProps } from 'antd';
import { InboxOutlined } from '@ant-design/icons';
import client from '../../../shared/api/client';

const { Dragger } = Upload;

export default function DocumentUploadPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [uploading, setUploading] = useState(false);
  const [uploadedFiles, setUploadedFiles] = useState<string[]>([]);

  const props: UploadProps = {
    name: 'file',
    multiple: false,
    showUploadList: true,
    accept: '.pdf,.docx,.doc,.pptx,.ppt,.txt,.md,.html,.csv',
    beforeUpload(file) {
      if (file.size > 50 * 1024 * 1024) {
        message.error(`${file.name} 超过 50MB 限制`);
        return Upload.LIST_IGNORE;
      }
      return true;
    },
    customRequest: async ({ file, onSuccess, onError, onProgress }) => {
      setUploading(true);
      try {
        const formData = new FormData();
        formData.append('file', file as File);
        const res = await client.post(`/knowledge/kb/${id}/upload`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
          onUploadProgress: (e) => {
            if (e.total && onProgress) onProgress({ percent: Math.round((e.loaded / e.total) * 100) });
          },
        }) as unknown as { success: boolean; data?: { docId: string; title: string }; error?: string };
        if (res?.success) {
          message.success(`${(file as File).name} 上传成功，正在处理中...`);
          setUploadedFiles((prev) => [...prev, (file as File).name]);
          onSuccess?.(res.data);
        } else {
          throw new Error(res?.error || '上传失败');
        }
      } catch (err: unknown) {
        message.error(err instanceof Error ? err.message : '上传失败');
        onError?.(err as Error);
      } finally {
        setUploading(false);
      }
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
          <p className="ant-upload-drag-icon"><InboxOutlined style={{ fontSize: 40, color: '#597ef7' }} /></p>
          <p className="ant-upload-text" style={{ fontSize: 14, fontWeight: 500 }}>点击或拖拽文件到此区域上传</p>
          <p className="ant-upload-hint" style={{ fontSize: 12, color: '#999' }}>支持 PDF、DOCX、PPTX、TXT、Markdown、HTML、CSV，最大 50MB</p>
        </Dragger>
        {uploadedFiles.length > 0 && (
          <div style={{ marginTop: 16 }}>
            {uploadedFiles.map((f) => <div key={f} style={{ fontSize: 13, color: '#16a34a', marginBottom: 4 }}>✅ {f} — 已上传，正在向量化处理中</div>)}
          </div>
        )}
        <div style={{ marginTop: 16, padding: '12px 16px', background: '#fffcf0', borderRadius: 8, fontSize: 12, color: '#d97706' }}>
          上传后系统将自动对文档进行分段和向量化处理，处理完成后可在知识库详情页检索。
        </div>
      </Card>
    </div>
  );
}
