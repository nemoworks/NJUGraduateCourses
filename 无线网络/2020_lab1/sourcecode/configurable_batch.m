
density=5;
anchor_num=5;
range=1;
s=10;

%%Mds density
X=zeros(10,100);
Y=zeros(10,100);
E=zeros(1,10);
R=zeros(1,10);
F=zeros(1,10);
D=[2 4 6 8 10 12 14 16 18 20];
for i=1:10
    density = 2*i;
    n=s*s*density;
    [ale,art,fc]=configurable("Mds",n,s,s,anchor_num,range);
    aale = sum(ale)/100;

    fprintf('Batch %d result:\n',i);
    fprintf('Average location error is: %f\n',aale);
    fprintf('Average running time is: %f\n',art);
    fprintf('Fuse Count: %d\n',fc);

    x = sort(ale.');
    mu = mean(x);
    sigma = std(x);
    y = cdf('Normal',x,mu,sigma);

    X(i,:)=x;
    Y(i,:)=y;
    E(1,i)=aale;
    R(1,i)=art;
    F(1,i)=fc;
end
plot(X(1,:),Y(1,:),X(2,:),Y(2,:),X(3,:),Y(3,:),X(4,:),Y(4,:),X(5,:),Y(5,:),X(6,:),Y(6,:),X(7,:),Y(7,:),X(8,:),Y(8,:),X(9,:),Y(9,:),X(10,:),Y(10,:));
pause;
plot(D,E,'r',D,R,'g',D,F,'b');
pause;
plot(D(1,2:10),E(1,2:10),'r',D(1,2:10),R(1,2:10),'g',D(1,2:10),F(1,2:10),'b');
pause;

%%Laplacian density
X=zeros(10,100);
Y=zeros(10,100);
E=zeros(1,10);
R=zeros(1,10);
F=zeros(1,10);
D=[2 4 6 8 10 12 14 16 18 20];
for i=1:10
    density = 2*i;
    n=s*s*density;
    [ale,art,fc]=configurable("lap",n,s,s,anchor_num,range);
    aale = sum(ale)/100;

    fprintf('Batch %d result:\n',i);
    fprintf('Average location error is: %f\n',aale);
    fprintf('Average running time is: %f\n',art);
    fprintf('Fuse Count: %d\n',fc);

    x = sort(ale.');
    mu = mean(x);
    sigma = std(x);
    y = cdf('Normal',x,mu,sigma);

    X(i,:)=x;
    Y(i,:)=y;
    E(1,i)=aale;
    R(1,i)=art;
    F(1,i)=fc;
end
plot(X(1,:),Y(1,:),X(2,:),Y(2,:),X(3,:),Y(3,:),X(4,:),Y(4,:),X(5,:),Y(5,:),X(6,:),Y(6,:),X(7,:),Y(7,:),X(8,:),Y(8,:),X(9,:),Y(9,:),X(10,:),Y(10,:));
pause;
plot(D,E,'r',D,R,'g',D,F,'b');
pause;
plot(D(1,2:10),E(1,2:10),'r',D(1,2:10),R(1,2:10),'g',D(1,2:10),F(1,2:10),'b');
pause;