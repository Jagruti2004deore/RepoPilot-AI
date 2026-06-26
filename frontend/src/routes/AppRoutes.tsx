import { lazy, Suspense } from 'react';
import { Route, Routes } from 'react-router-dom';
import { Loader } from '../components/common/Loader';
import { ProtectedRoute } from '../components/common/ProtectedRoute';
import { MainLayout } from '../layouts/MainLayout';
import { WorkspaceLayout } from '../layouts/WorkspaceLayout';

const Home = lazy(() => import('../pages/Home'));
const Login = lazy(() => import('../pages/Login'));
const Register = lazy(() => import('../pages/Register'));
const Workspace = lazy(() => import('../pages/Workspace'));
const Analysis = lazy(() => import('../pages/Analysis'));
const NotFound = lazy(() => import('../pages/NotFound'));

export function AppRoutes() {
  return (
    <Suspense fallback={<Loader />}>
      <Routes>
        <Route element={<MainLayout />}>
          <Route index element={<Home />} />
          <Route path="login" element={<Login />} />
          <Route path="register" element={<Register />} />
        </Route>
        <Route element={<ProtectedRoute />}>
          <Route element={<WorkspaceLayout />}>
            <Route path="workspace" element={<Workspace />} />
            <Route path="analysis/:repositoryId" element={<Analysis />} />
          </Route>
        </Route>
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Suspense>
  );
}
