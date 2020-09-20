function theta = gradientdescent(X, y, alpha, num_iters)
% function theta = gradientdescent(X, y, alpha, num_iters)
% Gradient descent function to solve the linear regression problem
% Input:
%   -- X: the training data
%   -- y: the expected output for each training sample
%   -- alpha: descent speed
% num_iters: number of iterations for the gradient descent
% Output:
%  -- theta: the set of theta values that has been trained

num_samples=size(y,1);
%initialize theta
theta=zeros(size(X,2),size(y,2));
for iter = 1:num_iters
    newtheta=zeros(size(X,2),size(y,2));
    for i=1:size(X,2)
       for j=1:size(y,2)
            newtheta(i,j)=theta(i,j)-alpha/num_samples*sum( (X*theta(:,j)-y(:,j)).*X(:,i));
       end
    end

    theta=newtheta;

end

end
